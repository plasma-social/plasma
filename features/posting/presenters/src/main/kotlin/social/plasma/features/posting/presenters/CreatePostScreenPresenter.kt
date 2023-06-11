package social.plasma.features.posting.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import shortBech32
import social.plasma.domain.InvokeError
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetUserSuggestions
import social.plasma.domain.interactors.SendNote
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.posting.screens.AutoCompleteSuggestion
import social.plasma.features.posting.screens.AutoCompleteSuggestion.UserSuggestion
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.features.posting.screens.ComposingScreen.NoteType
import social.plasma.features.posting.screens.CreatePostUiEvent
import social.plasma.features.posting.screens.CreatePostUiState
import social.plasma.models.NoteWithUser
import social.plasma.models.ProfileMention
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.utils.api.StringManager

class CreatePostScreenPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    private val sendNote: SendNote,
    private val noteRepository: NoteRepository,
    private val getUserSuggestions: GetUserSuggestions,
    private val getHashtagSuggestions: GetHashtagSuggestions,
    accountStateRepository: AccountStateRepository,
    observeMyMetadata: ObserveUserMetadata,
    @Assisted private val args: ComposingScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<CreatePostUiState> {

    private val myPubKey = PubKey(accountStateRepository.getPublicKey()!!.toByteString())
    private val avatarUrlFlow = observeMyMetadata.flow.map { it?.picture }
    private val isReply = when (args.noteType) {
        is NoteType.Reply, is NoteType.Repost -> true
        else -> false
    }

    init {
        observeMyMetadata(ObserveUserMetadata.Params(myPubKey))
    }

    @Composable
    override fun present(): CreatePostUiState {
        var submitting by remember { mutableStateOf(false) }
        var noteContent by remember {
            mutableStateOf(
                TextFieldValue(
                    args.content,
                    selection = TextRange(args.content.length)
                )
            )
        }
        val mentions = remember { mutableStateMapOf<String, ProfileMention>() }

        val suggestedUsers by remember { getUserSuggestions.flow }.collectAsState(emptyList())

        val rootNote by produceState<NoteWithUser?>(initialValue = null) {
            value = when (val noteType = args.noteType) {
                is NoteType.Reply -> noteRepository.getById(noteType.originalNote)
                is NoteType.Repost -> noteRepository.getById(noteType.originalNote)
                else -> null
            }
        }

        val buttonEnabled by produceState(false, noteContent, submitting, rootNote) {
            value = if (!isReply) {
                noteContent.text.isNotBlank() && !submitting
            } else {
                noteContent.text.isNotBlank() && !submitting && rootNote != null
            }
        }

        val avatarUrl by avatarUrlFlow.collectAsState(initial = null)

        val noteSubmitStatus by produceState<InvokeStatus?>(
            null,
            submitting,
            noteContent,
            rootNote
        ) {
            if (submitting) {
                value = sendNote.executeSync(
                    SendNote.Params(
                        content = noteContent.text,
                        parentNote = rootNote
                    )
                )
            }
        }

        val title by produceState(
            initialValue = if (isReply) stringManager[R.string.replying] else stringManager[R.string.new_note],
            rootNote
        ) {
            rootNote?.let { note ->
                value = stringManager.getFormattedString(
                    R.string.replying_to,
                    "op" to (note.userMetadataEntity?.userFacingName
                        ?: PubKey(note.noteEntity.pubkey.decodeHex()).shortBech32()),
                )
            }
        }

        LaunchedEffect(noteContent) {
            val lastWordBeforeCursor = noteContent.lastWordBeforeCursor
            val query =
                if (lastWordBeforeCursor.startsWith("@")) lastWordBeforeCursor.drop(1) else ""

            getUserSuggestions(GetUserSuggestions.Params(query))
        }

        val suggestedHashTags by produceState<List<String>>(emptyList(), noteContent) {
            value = fetchHashTagSuggestions(noteContent)
        }

        val autoCompleteSuggestions by produceState<List<AutoCompleteSuggestion>>(
            emptyList(),
            suggestedUsers,
            suggestedHashTags,
        ) {

            if (suggestedHashTags.isNotEmpty()) {
                value = suggestedHashTags.map {
                    AutoCompleteSuggestion.HashtagSuggestion(
                        hashTag = it,
                    )
                }
                return@produceState
            }

            if (suggestedUsers.isEmpty()) {
                value = emptyList()
                return@produceState
            }

            value = suggestedUsers.map {
                UserSuggestion(tagSuggestion = it)
            }
        }

        LaunchedEffect(noteSubmitStatus) {
            when (noteSubmitStatus) {
                is InvokeError -> {
                    submitting = false
                }

                InvokeSuccess -> {
                    navigator.pop()
                }

                else -> {
                    // no op
                }
            }
        }

        return CreatePostUiState(
            title = title,
            placeholder = stringManager[R.string.your_message],
            postButtonLabel = stringManager[R.string.post],
            avatarUrl = avatarUrl,
            postButtonEnabled = buttonEnabled,
            showAutoComplete = autoCompleteSuggestions.isNotEmpty(),
            mentions = mentions,
            noteContent = noteContent,
            autoCompleteSuggestions = autoCompleteSuggestions,
            replyingTo = when (val noteType = args.noteType) {
                is NoteType.Reply -> noteType.originalNote
                is NoteType.Repost -> noteType.originalNote
                else -> null
            },
        ) { event ->
            when (event) {
                CreatePostUiEvent.OnBackClick -> navigator.pop()
                is CreatePostUiEvent.OnNoteChange -> noteContent = event.content
                CreatePostUiEvent.OnSubmitPost -> {
                    submitting = true
                }

                is CreatePostUiEvent.OnUserSuggestionTapped -> {
                    val profileMention = ProfileMention(
                        text = "@${event.suggestion.title}",
                        pubkey = event.suggestion.pubKey,
                    )
                    val replacement = "@${profileMention.pubkey.npub}"

                    mentions[replacement] = profileMention

                    noteContent =
                        noteContent.replaceTextForCurrentMention(
                            "@",
                            "$replacement "
                        ) // trailing space to force the cursor to start a new "word"
                }

                is CreatePostUiEvent.OnHashTagSuggestionTapped -> {
                    noteContent =
                        noteContent.replaceTextForCurrentMention("#", "${event.hashtag} ")
                }
            }
        }
    }

    private suspend fun fetchHashTagSuggestions(noteContent: TextFieldValue): List<String> {
        val lastWordBeforeCursorPosition = noteContent.lastWordBeforeCursor

        val suggestions =
            if (lastWordBeforeCursorPosition.startsWith("#") && lastWordBeforeCursorPosition.length > 1) {
                getHashtagSuggestions.executeSync(
                    GetHashtagSuggestions.Params(lastWordBeforeCursorPosition.drop(1))
                ).map { "#$it" }
            } else {
                emptyList()
            }
        return suggestions
    }

    private val TextFieldValue.lastWordBeforeCursor: String
        get() = getTextBeforeSelection(text.length).text
            .replace("\n", " ")
            .split(" ")
            .lastOrNull() ?: ""

    private fun TextFieldValue.replaceTextForCurrentMention(
        mentionDelimiter: String,
        replacement: String,
    ): TextFieldValue {
        val cursorPosition = selection.end
        val contentBeforeCursor = text.substring(0, cursorPosition)
        val contentAfterCursor = text.substring(cursorPosition, text.length)

        val contentBeforeCurrentMention = contentBeforeCursor.substringBeforeLast(mentionDelimiter)

        val updatedText = buildString {
            append(contentBeforeCurrentMention)
            append(replacement)
            append(contentAfterCursor)
        }

        val newCursorPosition = contentBeforeCurrentMention.length + replacement.length

        return copy(
            text = updatedText,
            selection = TextRange(newCursorPosition)
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(args: ComposingScreen, navigator: Navigator): CreatePostScreenPresenter
    }
}



