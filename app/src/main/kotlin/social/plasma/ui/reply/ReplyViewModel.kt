package social.plasma.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.db.notes.NoteWithUser
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.EventsEffect
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.post.PostUiEvent
import social.plasma.ui.post.PostUiState
import javax.inject.Inject

@HiltViewModel
class ReplyViewModel @Inject constructor(
    val noteRepository: NoteRepository,
    recompositionClock: RecompositionClock,
    savedStateHandle: SavedStateHandle,
) : MoleculeViewModel<PostUiState, PostUiEvent>(recompositionClock) {
    val noteId: String = savedStateHandle["noteId"]!!

    @Composable
    override fun models(events: Flow<PostUiEvent>): PostUiState {
        val parentNote = remember { mutableStateOf<NoteWithUser?>(null) }
        var content by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            parentNote.value = noteRepository.getById(noteId)
        }

        EventsEffect(events) { event ->
            when (event) {
                is PostUiEvent.OnNoteChange -> content = event.content
                PostUiEvent.PostNote -> launch { onCreateReply(content) }
            }
        }

        return PostUiState(
            postEnabled = content.isNotBlank(),
            parentNote = parentNote.value,
        )
    }

    private suspend fun onCreateReply(content: String) {
        noteRepository.replyToNote(noteId, content)
    }
}
