package social.plasma.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.MoleculeViewModel
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val notesRepository: NoteRepository,
    recompositionClock: RecompositionClock,
) : MoleculeViewModel<PostUiState>(recompositionClock) {

    private var state = mutableStateOf(PostUiState())

    @Composable
    override fun models(): PostUiState {

        return state.value
    }

    fun onNoteChange(note: String) {
        state.value = PostUiState(postEnabled = note.isNotBlank())
    }

    fun onPostNote(note: String) {
        viewModelScope.launch {
            notesRepository.postNote(note)
        }
    }

}