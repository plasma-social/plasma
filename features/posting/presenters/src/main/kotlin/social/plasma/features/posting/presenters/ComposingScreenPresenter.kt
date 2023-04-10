package social.plasma.features.posting.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import social.plasma.domain.InvokeError
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.GetNoteTagSuggestions
import social.plasma.domain.interactors.SendNote
import social.plasma.features.posting.screens.AutoCompleteSuggestion
import social.plasma.features.posting.screens.ComposePostUiEvent
import social.plasma.features.posting.screens.ComposePostUiState
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.models.NoteWithUser
import social.plasma.models.ProfileMention
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.utils.api.StringManager

class ComposingScreenPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    private val sendNote: SendNote,
    private val noteRepository: NoteRepository,
    private val getNoteTagSuggestions: GetNoteTagSuggestions,
    private val getNip5Status: GetNip5Status,
    @Assisted private val args: ComposingScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ComposePostUiState> {
    private val isReply = args.parentNote != null

    @Composable
    override fun present(): ComposePostUiState {
        var submitting by remember { mutableStateOf(false) }
        var noteContent by remember { mutableStateOf(TextFieldValue()) }
        val mentions = remember { mutableStateMapOf<String, ProfileMention>() }

        val rootNote by produceState<NoteWithUser?>(initialValue = null) {
            args.parentNote?.let { noteId ->
                value = noteRepository.getById(noteId)
            }
        }

        val buttonEnabled by produceState(false, noteContent, submitting, rootNote) {
            value = if (!isReply) {
                noteContent.text.isNotBlank() && !submitting
            } else {
                noteContent.text.isNotBlank() && !submitting && rootNote != null
            }
        }

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
            initialValue = if (isReply) "Replying" else stringManager[R.string.new_note],
            rootNote
        ) {
            rootNote?.let { note ->
                value =
                    "Replying to ${note.userMetadataEntity?.userFacingName ?: note.noteEntity.pubkey.decodeHex()}"
            }
        }

        val suggestedUsers by produceState<List<TagSuggestion>>(
            initialValue = emptyList(),
            noteContent
        ) {
            val cursorPosition =
                if (noteContent.selection.collapsed) noteContent.selection.start else 0

            value =
                getNoteTagSuggestions.executeSync(
                    GetNoteTagSuggestions.Params(
                        noteContent.text,
                        cursorPosition
                    )
                )
        }

        val autoCompleteSuggestions by produceState<List<AutoCompleteSuggestion>>(
            emptyList(),
            suggestedUsers
        ) {

            val list = suggestedUsers.map {
                AutoCompleteSuggestion(
                    tagSuggestion = it,
                    nip5Valid = if (it.nip5Identifier == null) false else null
                )
            }.toMutableStateList()

            suggestedUsers.forEachIndexed { index, suggestion ->
                val nip5Identifier = suggestion.nip5Identifier ?: return@forEachIndexed
                if (nip5Identifier.trim().isEmpty()) return@forEachIndexed

                launch {
                    val nip5Status = getNip5Status.executeSync(
                        GetNip5Status.Params(
                            suggestion.pubKey,
                            nip5Identifier
                        )
                    ).isValid()

                    list[index] = AutoCompleteSuggestion(suggestion, nip5Status)
                }
            }

            value = list
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

        return ComposePostUiState(
            title = title,
            placeholder = stringManager[R.string.your_message],
            postButtonLabel = stringManager[R.string.post],
            postButtonEnabled = buttonEnabled,
            showAutoComplete = autoCompleteSuggestions.isNotEmpty(),
            mentions = mentions,
            noteContent = noteContent,
            autoCompleteSuggestions = autoCompleteSuggestions,
        ) { event ->
            when (event) {
                ComposePostUiEvent.OnBackClick -> navigator.pop()
                is ComposePostUiEvent.OnNoteChange -> noteContent = event.content
                ComposePostUiEvent.OnSubmitPost -> {
                    submitting = true
                }

                is ComposePostUiEvent.OnSuggestionTapped -> {
                    val profileMention = ProfileMention(
                        text = "@${event.suggestion.title}",
                        pubkey = event.suggestion.pubKey,
                    )
                    val replacement = "@${profileMention.pubkey.npub}"

                    mentions[replacement] = profileMention

                    noteContent =
                        noteContent.replaceTextForCurrentMention("$replacement ") // trailing space to force the cursor to start a new "word"
                }
            }
        }
    }

    private fun TextFieldValue.replaceTextForCurrentMention(replacement: String): TextFieldValue {
        val cursorPosition = selection.end
        val contentBeforeCursor = text.substring(0, cursorPosition)
        val contentAfterCursor = text.substring(cursorPosition, text.length)

        val contentBeforeCurrentMention = contentBeforeCursor.substringBeforeLast("@")

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
        fun create(args: ComposingScreen, navigator: Navigator): ComposingScreenPresenter
    }
}



