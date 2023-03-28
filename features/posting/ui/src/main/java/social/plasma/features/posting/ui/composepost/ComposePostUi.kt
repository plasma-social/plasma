package social.plasma.features.posting.ui.composepost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import social.plasma.features.posting.screens.ComposePostUiEvent
import social.plasma.features.posting.screens.ComposePostUiEvent.OnSuggestionTapped
import social.plasma.features.posting.screens.ComposePostUiState
import social.plasma.ui.R
import social.plasma.ui.components.Avatar

class ComposePostUi : Ui<ComposePostUiState> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        state: ComposePostUiState, modifier: Modifier,
    ) {
        val onEvent = state.onEvent

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
                CenterAlignedTopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { onEvent(ComposePostUiEvent.OnBackClick) }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_back),
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        Button(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = { onEvent(ComposePostUiEvent.OnSubmitPost) },
                            enabled = state.postButtonEnabled
                        ) {
                            Text(state.postButtonLabel)
                        }
                    }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    value = state.noteContent,
                    onValueChange = { newValue ->
                        if (newValue != state.noteContent) {
                            onEvent(ComposePostUiEvent.OnNoteChange(newValue))
                        }
                    },
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
                if (state.showTagSuggestions) {
                    Divider()
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.tagSuggestions) { suggestion ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    onEvent(
                                        OnSuggestionTapped(
                                            suggestion
                                        )
                                    )
                                },
                                headlineContent = {
                                    Text(suggestion.title)
                                },
                                supportingContent = suggestion.subtitle?.let { subtitle ->
                                    {
                                        Text(subtitle)
                                    }
                                },
                                leadingContent = {
                                    Avatar(
                                        imageUrl = suggestion.imageUrl,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

