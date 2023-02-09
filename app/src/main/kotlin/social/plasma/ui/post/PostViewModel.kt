package social.plasma.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.molecule.RecompositionClock
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.EventsEffect
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.base.ViewModelWithNavigatorFactory
import social.plasma.ui.navigation.Navigator

class PostViewModel @AssistedInject constructor(
    private val notesRepository: NoteRepository,
    recompositionClock: RecompositionClock,
    @Assisted private val navigator: Navigator,
) : MoleculeViewModel<PostUiState, PostUiEvent>(recompositionClock) {

    @Composable
    override fun models(events: Flow<PostUiEvent>): PostUiState {
        var noteContent by remember { mutableStateOf("") }

        EventsEffect(events) { event ->
            when (event) {
                is PostUiEvent.OnNoteChange -> noteContent = event.content
                is PostUiEvent.PostNote -> launch { postNote(noteContent) }
            }
        }

        return PostUiState(
            postEnabled = noteContent.isNotBlank(),
        )
    }

    private suspend fun postNote(note: String) {
        notesRepository.postNote(note)
        navigator.goBack()
    }

    @AssistedFactory
    interface Factory : ViewModelWithNavigatorFactory<PostViewModel> {
        override fun create(navigator: Navigator): PostViewModel
    }
}