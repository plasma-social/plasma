package social.plasma.feeds.presenters.eventfeed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiEvent
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.models.EventModel
import social.plasma.shared.utils.api.StringManager
import kotlin.math.min

class EventFeedPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    @Assisted private val navigator: Navigator,
    @Assisted private val pagingData: Flow<PagingData<EventModel>>,
) : Presenter<EventFeedUiState> {

    @Composable
    override fun present(): EventFeedUiState {
        val listState = rememberLazyListState()

        val currentVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

        var currentFeedItemCount by rememberRetained { mutableStateOf(0) }

        val initialFeedCount by produceRetainedState(
            initialValue = 0,
            currentFeedItemCount,
            currentVisibleIndex
        ) {
            if (value == 0) {
                value = currentFeedItemCount
            }

            if (currentVisibleIndex <= currentFeedItemCount - value) {
                value = currentFeedItemCount - currentVisibleIndex
            }
        }

        val unseenItemCount by produceRetainedState(
            initialValue = 0,
            currentVisibleIndex,
            currentFeedItemCount,
            initialFeedCount
        ) {
            value = min(currentVisibleIndex, currentFeedItemCount - initialFeedCount)
        }

        val refreshText = remember(unseenItemCount) {
            if (unseenItemCount < NOTE_COUNT_MAX) {
                stringManager.getFormattedString(
                    R.string.new_notes_count,
                    mapOf("count" to unseenItemCount)
                )
            } else {
                stringManager.getFormattedString(
                    R.string.many_new_notes,
                    mapOf("count" to "$NOTE_COUNT_MAX+")
                )
            }
        }

        val coroutineScope = rememberCoroutineScope()

        return EventFeedUiState(
            listState = listState,
            items = pagingData,
            displayRefreshButton = unseenItemCount > 0,
            refreshText = refreshText,
        ) { event ->
            when (event) {
                is EventFeedUiEvent.OnFeedCountChange -> currentFeedItemCount = event.itemCount
                EventFeedUiEvent.OnRefreshButtonClick -> coroutineScope.launch {
                    listState.scrollToItem(0)
                }

                is EventFeedUiEvent.OnChildNavEvent -> navigator.onNavEvent(event.navEvent)
            }
        }
    }

    companion object {
        private const val NOTE_COUNT_MAX = 99
    }

    @AssistedFactory
    interface Factory {
        fun create(
            navigator: Navigator,
            pagingData: Flow<PagingData<EventModel>>,
        ): EventFeedPresenter
    }

}
