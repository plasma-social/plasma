package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import social.plasma.db.notes.NoteDao
import social.plasma.models.PubKey
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.ext.noteCardsPagingFlow
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    private val noteDao: NoteDao,
    private val relays: Relays,
) : MoleculeViewModel<FeedUiState>(recompositionClock) {
    private val feedReactionsSubscriptions: AtomicReference<Map<String, List<UnsubscribeMessage>>> =
        AtomicReference(
            mapOf()
        )

    private val feedPagingFlow = noteCardsPagingFlow { noteDao.allNotesWithUsersPagingSource() }
    private val globalFeedSubscription =
        relays.subscribe(SubscribeMessage(filters = Filters.globalFeedNotes))

    @Composable
    override fun models(): FeedUiState {
        return FeedUiState.Loaded(feedPagingFlow = feedPagingFlow)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.Default) {
            globalFeedSubscription.forEach { relays.unsubscribe(it) }
            feedReactionsSubscriptions.get().forEach {
                it.value.forEach { relays.unsubscribe(it) }
            }
        }
    }

    fun onNoteDisposed(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            feedReactionsSubscriptions.updateAndGet { currentMap ->
                currentMap[id]?.let { it.forEach { relays.unsubscribe(it) } }
                currentMap - id
            }
        }
    }

    fun onNoteDisplayed(id: String, pubkey: PubKey) {
        viewModelScope.launch(Dispatchers.Default) {
            feedReactionsSubscriptions.updateAndGet {
                val newSubscriptions =
                    relays.subscribe(SubscribeMessage(filters = Filters.noteReactions(id))) +
                            relays.subscribe(
                                SubscribeMessage(filters = Filters.userMetaData(pubkey.value))
                            )
                it + (id to newSubscriptions)
            }
        }
    }
}
