package social.plasma.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.navigation.Navigator

class PostViewModel @AssistedInject constructor(
    private val notesRepository: NoteRepository,
    recompositionClock: RecompositionClock,
    @Assisted private val navigator: Navigator,
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
            navigator.goBack()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): PostViewModel
    }
}