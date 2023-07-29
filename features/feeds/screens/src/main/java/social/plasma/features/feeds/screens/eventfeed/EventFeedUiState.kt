package social.plasma.features.feeds.screens.eventfeed

import androidx.compose.foundation.lazy.LazyListState
import androidx.paging.PagingData
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import social.plasma.models.EventModel

data class EventFeedUiState(
    val listState: LazyListState,
    val items: Flow<PagingData<EventModel>>,
    val refreshText: String,
    val displayRefreshButton: Boolean,
    val onEvent: (EventFeedUiEvent) -> Unit,
) : CircuitUiState {
    companion object {
        val Empty = EventFeedUiState(
            listState = LazyListState(),
            items = emptyFlow(),
            refreshText = "",
            displayRefreshButton = false,
            onEvent = {}
        )
    }
}
