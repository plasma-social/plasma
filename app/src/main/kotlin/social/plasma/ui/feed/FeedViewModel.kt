package social.plasma.ui.feed

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import social.plasma.models.Note
import social.plasma.relay.Relays
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    relays: Relays
) : ViewModel() {
    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val relay = relays.relay("wss://relay.damus.io")

    // TODO only subscribe to relays once during the entire app cycle
    // https://github.com/nostr-protocol/nips/blob/master/01.md#other-notes
    private val relaySubscription: Flow<Note> = relay.flowNotes()

    val uiState: StateFlow<FeedListUiState> = moleculeScope.launchMolecule(recompositionClock) {
        // TODO move this to some type of repository that can aggregate the list
        val messageList = remember { mutableStateListOf<Note>() }
        val newMessage by relaySubscription.collectAsState(initial = null)

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                relay.connectAndSubscribe()
            }
        }

        LaunchedEffect(newMessage) {
            newMessage?.let { messageList.add(0, it) }
        }

        if (messageList.isEmpty()) {
            FeedListUiState.Loading
        } else {
            FeedListUiState.Loaded(messageList)
        }
    }
}