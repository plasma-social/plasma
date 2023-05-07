package social.plasma.features.posting.ui.composepost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.slack.circuit.Ui
import social.plasma.features.posting.screens.AutoCompleteSuggestion
import social.plasma.features.posting.screens.CreatePostUiEvent
import social.plasma.features.posting.screens.CreatePostUiEvent.OnHashTagSuggestionTapped
import social.plasma.features.posting.screens.CreatePostUiEvent.OnUserSuggestionTapped
import social.plasma.features.posting.screens.CreatePostUiState
import social.plasma.ui.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.Nip5Badge
import javax.inject.Inject

class CreatePostScreenUi @Inject constructor() : Ui<CreatePostUiState> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        state: CreatePostUiState, modifier: Modifier,
    ) {
        val onEvent = state.onEvent

        val linkColor = MaterialTheme.colorScheme.primary

        val visualTransformation = remember(state.mentions) {
            MentionsVisualTransformation(
                linkColor, state.mentions
            )
        }

        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column {
                CenterAlignedTopAppBar(title = { Text(state.title) }, navigationIcon = {
                    IconButton(onClick = { onEvent(CreatePostUiEvent.OnBackClick) }) {
                        Icon(
                            painterResource(R.drawable.ic_chevron_back),
                            contentDescription = null,
                        )
                    }
                }, actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = { onEvent(CreatePostUiEvent.OnSubmitPost) },
                        enabled = state.postButtonEnabled
                    ) {
                        Text(state.postButtonLabel)
                    }
                })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                ) {
                    Avatar(
                        modifier = Modifier.padding(top = 8.dp),
                        imageUrl = state.avatarUrl,
                        contentDescription = null
                    )
                    TextField(
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        value = state.noteContent,
                        onValueChange = { newValue ->
                            if (newValue != state.noteContent) {
                                onEvent(CreatePostUiEvent.OnNoteChange(newValue))
                            }
                        },
                        visualTransformation = visualTransformation,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        placeholder = { Text(state.placeholder) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true,
                            keyboardType = KeyboardType.Ascii,
                        ),
                    )
                }

                if (state.showAutoComplete) {
                    Divider()
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.autoCompleteSuggestions) { suggestion ->
                            when (suggestion) {
                                is AutoCompleteSuggestion.HashtagSuggestion -> HashtagSuggestion(
                                    suggestion,
                                    onEvent
                                )

                                is AutoCompleteSuggestion.UserSuggestion -> UserSuggestion(
                                    suggestion,
                                    onEvent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HashtagSuggestion(
        suggestion: AutoCompleteSuggestion.HashtagSuggestion,
        onEvent: (CreatePostUiEvent) -> Unit,
    ) {
        ListItem(modifier = Modifier.clickable {
            onEvent(
                OnHashTagSuggestionTapped(
                    suggestion.hashTag
                )
            )
        }, headlineContent = {
            Text(suggestion.hashTag)
        })
    }

    @Composable
    private fun UserSuggestion(
        userSuggestion: AutoCompleteSuggestion.UserSuggestion,
        onEvent: (CreatePostUiEvent) -> Unit,
    ) {
        val (suggestion) = userSuggestion

        ListItem(modifier = Modifier.clickable {
            onEvent(
                OnUserSuggestionTapped(
                    suggestion
                )
            )
        }, headlineContent = {
            Text(suggestion.title)
        }, supportingContent = {
            suggestion.nip5Identifier?.takeIf { it.isNotEmpty() }
                ?.let {
                    Nip5Badge(
                        identifier = it,
                        nip5Valid = suggestion.isNip5Valid
                    )
                }
        }, leadingContent = {
            Avatar(
                imageUrl = suggestion.imageUrl, contentDescription = null
            )
        })
    }
}
