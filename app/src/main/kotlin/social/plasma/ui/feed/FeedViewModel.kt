package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import social.plasma.PubKey
import social.plasma.repository.NoteRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.ext.noteCardsPagingFlow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    private val noteRepository: NoteRepository,
    private val userMetaDataRepository: UserMetaDataRepository,
) : MoleculeViewModel<FeedUiState>(recompositionClock) {
    private val feedPagingFlow = noteCardsPagingFlow(noteRepository.observeContactsNotes())

    @Composable
    override fun models(): FeedUiState {
        return FeedUiState.Loaded(feedPagingFlow = feedPagingFlow)
    }

    fun onNoteDisposed(id: String) {
        // TODO unsubscribe
    }

    fun onNoteDisplayed(id: String, pubkey: PubKey) {
        // TODO do this using a single subscription for all notes
    }
}
