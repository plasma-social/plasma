package social.plasma.ui.feed

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
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

    // TODO only subscribe to relays once during the entire app cycle
    // https://github.com/nostr-protocol/nips/blob/master/01.md#other-notes
    private val relaySubscription: Flow<Note> = runBlocking {
        relays.relay("wss://relay.damus.io").connectAndSubscribe().flowNotes()
    }

    val uiState: StateFlow<FeedListUiState> = moleculeScope.launchMolecule(recompositionClock) {
        // TODO move this to some type of repository that can aggregate the list
        val messageList = remember { mutableStateListOf<Note>() }
        val newMessage by relaySubscription.collectAsState(initial = null)

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