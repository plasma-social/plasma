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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
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
    private val relaySubscription =
        relays.subscribe("wss://relay.damus.io").filter { it.contains("kind\":1") }

    val uiState: StateFlow<FeedListUiState> = moleculeScope.launchMolecule(recompositionClock) {
        // TODO move this to some type of repository that can aggregate the list
        val messageList = remember { mutableStateListOf<Note>() }
        val newMessage by relaySubscription.collectAsState(initial = null)

        LaunchedEffect(newMessage) {
            // TODO filter at repository level using strongly typed classes
            newMessage?.let {
                messageList.add(0, Note(it))
            }
        }

        if (messageList.isEmpty()) {
            FeedListUiState.Loading
        } else {
            FeedListUiState.Loaded(messageList)
        }
    }
}