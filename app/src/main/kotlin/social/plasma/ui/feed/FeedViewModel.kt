package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import social.plasma.db.notes.NoteDao
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.ext.noteCardsPagingFlow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    private val noteDao: NoteDao,
    private val relays: Relays,
) : MoleculeViewModel<FeedUiState>(recompositionClock) {

    private val feedPagingFlow = noteCardsPagingFlow { noteDao.allNotesWithUsersPagingSource() }
    private val unsubscribeMessage =
        relays.subscribe(SubscribeMessage(filters = Filters.globalFeedNotes))

    @Composable
    override fun models(): FeedUiState {
        return FeedUiState.Loaded(feedPagingFlow = feedPagingFlow)
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeMessage.forEach {
            relays.unsubscribe(it)
        }
    }
}
