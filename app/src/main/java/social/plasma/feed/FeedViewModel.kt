package social.plasma.feed

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock.ContextClock
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import social.plasma.models.Note
import social.plasma.relay.Relays
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {
    private val relays = Relays(OkHttpClient(), Dispatchers.IO)

    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val relaySubscription = relays.subscribe("wss://relay.damus.io")

    val uiState: StateFlow<FeedListUiState> = moleculeScope.launchMolecule(ContextClock) {
        // TODO move this to some type of repository that can aggregate the list
        val messageList = remember { mutableStateListOf<Note>() }
        val newMessage by relaySubscription.collectAsState(initial = "")

        if (newMessage.isNotBlank()) {
            messageList.add(Note(newMessage))
        }

        if (messageList.isEmpty()) {
            FeedListUiState.Loading
        } else {
            FeedListUiState.Loaded(messageList)
        }
    }
}