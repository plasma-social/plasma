package social.plasma.features.posting.ui.composepost

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.delay
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.features.posting.screens.AutoCompleteSuggestion
import social.plasma.features.posting.screens.CreatePostUiEvent
import social.plasma.features.posting.screens.CreatePostUiEvent.OnHashTagSuggestionTapped
import social.plasma.features.posting.screens.CreatePostUiEvent.OnUserSuggestionTapped
import social.plasma.features.posting.screens.CreatePostUiState
import social.plasma.ui.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.withHapticFeedBack
import social.plasma.ui.theme.PlasmaTheme
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

        Scaffold(
            modifier = modifier
                .fillMaxSize(),
            contentWindowInsets = WindowInsets.ime,
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
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
                        onClick = withHapticFeedBack { onEvent(CreatePostUiEvent.OnSubmitPost) },
                        enabled = state.postButtonEnabled
                    ) {
                        Text(state.postButtonLabel)
                    }
                })
            },
        ) { paddingValues ->
            val lazyListState = rememberLazyListState()

            LaunchedEffect(Unit) {
                delay(500)

                if (state.replyingTo != null) {
                    lazyListState.animateScrollToItem(1, -50)
                }
                focusRequester.requestFocus()
            }

            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                state = lazyListState,
            ) {

                state.replyingTo?.let { noteId ->
                    item("embedded-note") {
                        CircuitContent(
                            QuotedNoteScreen(noteId),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                            )
                        )
                    }
                }

                item("text-field") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Avatar(
                            modifier = Modifier.padding(top = 8.dp),
                            imageUrl = state.avatarUrl,
                            contentDescription = null
                        )
                        TextField(
                            modifier = Modifier
                                .then(
                                    if (state.showAutoComplete) {
                                        Modifier
                                    } else {
                                        Modifier.fillParentMaxHeight()
                                    }
                                )
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
                }

                if (state.showAutoComplete) {
                    item("auto-complete-suggestions") {
                        Box(modifier = Modifier.fillParentMaxHeight()) {
                            AutoCompleteSuggestions(
                                modifier = Modifier,
                                state.autoCompleteSuggestions,
                                onEvent,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun AutoCompleteSuggestions(
        modifier: Modifier = Modifier,
        autoCompleteSuggestions: List<AutoCompleteSuggestion>,
        onEvent: (CreatePostUiEvent) -> Unit,
    ) {
        LazyColumn(
            modifier = modifier,
        ) {
            if (autoCompleteSuggestions.isNotEmpty()) {
                stickyHeader {
                    Divider()
                }
            }
            items(autoCompleteSuggestions) { suggestion ->
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
            Nip5Badge(suggestion.nip5Status)
        }, leadingContent = {
            Avatar(
                imageUrl = suggestion.imageUrl, contentDescription = null
            )
        })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreatePostUi() {
    PlasmaTheme {
        CreatePostScreenUi().Content(
            CreatePostUiState(
                title = "Create Post",
                noteContent = TextFieldValue("This is a test post"),
                placeholder = "What's on your mind?",
                postButtonLabel = "Post",
                postButtonEnabled = true,
                onEvent = {}
            ), modifier = Modifier.fillMaxSize()
        )
    }
}
