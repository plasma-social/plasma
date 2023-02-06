package social.plasma.ui.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.post.PostUiState
import javax.inject.Inject

@HiltViewModel
class ReplyViewModel @Inject constructor(
    val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
    recompositionClock: RecompositionClock,
) : MoleculeViewModel<PostUiState>(recompositionClock) {
    val noteId: String = savedStateHandle["noteId"]!!

    private val state = mutableStateOf(PostUiState())

    init {
        viewModelScope.launch {
            val note = noteRepository.getById(noteId)

            state.value = state.value.copy(
                parentNote = note,
            )
        }
    }

    @Composable
    override fun models(): PostUiState {
        return state.value
    }

    fun onNoteChange(note: String) {
        state.value = state.value.copy(postEnabled = note.isNotBlank())
    }

    fun onCreateReply(navHostController: NavHostController, content: String) {
        viewModelScope.launch {
            noteRepository.replyToNote(noteId, content)
            navHostController.popBackStack()
        }
    }
}
