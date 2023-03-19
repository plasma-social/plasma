package social.plasma.features.posting.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import okio.ByteString.Companion.decodeHex
import social.plasma.data.daos.NotesDao
import social.plasma.domain.InvokeError
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.domain.interactors.SendNote
import social.plasma.features.posting.screens.ComposePostUiEvent
import social.plasma.features.posting.screens.ComposePostUiState
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.models.NoteWithUser
import social.plasma.shared.utils.api.StringManager

class ComposingScreenPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    private val sendNote: SendNote,
    private val notesDao: NotesDao,
    @Assisted private val args: ComposingScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ComposePostUiState> {
    private val isReply = args.parentNote != null

    @Composable
    override fun present(): ComposePostUiState {
        var submitting by remember { mutableStateOf(false) }
        var noteContent by remember { mutableStateOf("") }

        val rootNote by produceState<NoteWithUser?>(initialValue = null) {
            args.parentNote?.hex?.let { noteId ->
                value = notesDao.getById(noteId)
            }
        }

        val buttonEnabled by produceState(false, noteContent, submitting, rootNote) {
            value = if (!isReply) {
                noteContent.isNotBlank() && !submitting
            } else {
                noteContent.isNotBlank() && !submitting && rootNote != null
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
                        content = noteContent,
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
        ) { event ->
            when (event) {
                ComposePostUiEvent.OnBackClick -> navigator.pop()
                is ComposePostUiEvent.OnNoteChange -> noteContent = event.content
                ComposePostUiEvent.OnSubmitPost -> {
                    submitting = true
                }
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(args: ComposingScreen, navigator: Navigator): ComposingScreenPresenter
    }
}

