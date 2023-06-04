package social.plasma.feeds.presenters.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.RepostNote
import social.plasma.domain.interactors.SendNoteReaction
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.models.HashTag
import social.plasma.models.NoteWithUser
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.min

class FeedPresenter @AssistedInject constructor(
    private val sendNoteReaction: SendNoteReaction,
    private val repostNote: RepostNote,
    private val syncMetadata: SyncMetadata,
    private val stringManager: StringManager,
    private val openGraphParser: OpenGraphParser,
    private val notePagingFlowMapper: NotePagingFlowMapper,
    @Assisted private val pagingFlow: Flow<PagingData<NoteWithUser>>,
    @Assisted private val navigator: Navigator,
) : Presenter<FeedUiState> {

    private val getOpenGraphMetadata: suspend (String) -> OpenGraphMetadata? =
        {
            try {
                openGraphParser.parse(URL(it))
            } catch (e: MalformedURLException) {
                Timber.w(e)
                null
            }
        }

    @Composable
    override fun present(): FeedUiState {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
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

        val transformedPagingFlow = remember(pagingFlow) {
            notePagingFlowMapper.map(pagingFlow)
        }

        return FeedUiState(
            pagingFlow = transformedPagingFlow,
            refreshText = refreshText,
            displayRefreshButton = unseenItemCount > 0,
            listState = listState,
            getOpenGraphMetadata = getOpenGraphMetadata
        ) { event ->
            when (event) {
                is FeedUiEvent.OnNoteClick -> navigator.goTo(ThreadScreen(event.noteId))
                is FeedUiEvent.OnReplyClick -> navigator.goTo(ComposingScreen(parentNote = event.noteId))
                is FeedUiEvent.OnNoteRepost -> {
                    coroutineScope.launch {
                        repostNote.executeSync(RepostNote.Params(noteId = event.noteId))
                    }
                }

                is FeedUiEvent.OnNoteReaction -> {
                    coroutineScope.launch {
                        sendNoteReaction.executeSync(SendNoteReaction.Params(noteId = event.noteId))
                    }
                }

                is FeedUiEvent.OnProfileClick -> {
                    navigator.goTo(ProfileScreen(pubKeyHex = event.pubKey.key.hex()))
                }

                is FeedUiEvent.OnNoteDisplayed -> {
                    coroutineScope.launch {
                        syncMetadata.executeSync(SyncMetadata.Params(event.pubKey))
                    }
                }

                is FeedUiEvent.OnFeedCountChange -> {
                    currentFeedItemCount = event.count
                }

                FeedUiEvent.OnRefreshButtonClick -> {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }

                is FeedUiEvent.OnHashTagClick -> {
                    navigator.goTo(HashTagFeedScreen(HashTag.parse(event.hashTag)))
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, pagingFlow: Flow<PagingData<NoteWithUser>>): FeedPresenter
    }

    companion object {
        private const val NOTE_COUNT_MAX = 50
    }
}
