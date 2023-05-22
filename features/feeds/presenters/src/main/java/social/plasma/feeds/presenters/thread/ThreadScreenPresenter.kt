package social.plasma.feeds.presenters.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.paging.PagingConfig
import androidx.paging.map
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.SyncThreadEvents
import social.plasma.domain.observers.ObservePagedThreadFeed
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.threads.ThreadItem
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.feeds.screens.threads.ThreadScreenUiEvent
import social.plasma.features.feeds.screens.threads.ThreadScreenUiState
import social.plasma.feeds.presenters.feed.FeedPresenter
import social.plasma.feeds.presenters.feed.NotePagingFlowMapper
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

class ThreadScreenPresenter @AssistedInject constructor(
    feedPresenterFactory: FeedPresenter.Factory,
    private val observePagedThreadFeed: ObservePagedThreadFeed,
    private val syncThreadEvents: SyncThreadEvents,
    feedMapper: NotePagingFlowMapper,
    private val stringManager: StringManager,
    private val openGraphParser: OpenGraphParser,
    @Assisted private val args: ThreadScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ThreadScreenUiState> {
    private val pagingFlow = observePagedThreadFeed.flow.onStart {
        observePagedThreadFeed(
            ObservePagedThreadFeed.Params(
                noteId = args.noteId,
                pagingConfig = PagingConfig(
                    pageSize = 10,
                )
            )
        )
    }

    private val feedPresenter = feedPresenterFactory.create(navigator, feedMapper.map(pagingFlow))
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
    override fun present(): ThreadScreenUiState {
        LaunchedEffect(Unit) {
            syncThreadEvents.executeSync(SyncThreadEvents.Params(args.noteId))
        }

        val feedPresenterState = feedPresenter.present()

        val pagingFlow by produceState(
            initialValue = emptyFlow(),
            feedPresenterState.pagingFlow
        ) {
            var rootAdded = false
            value = feedPresenterState.pagingFlow.map { pagingData ->
                pagingData.map { feedItem ->
                    when (feedItem) {
                        is FeedItem.NoteCard -> {
                            if (feedItem.id == args.noteId.hex) {
                                ThreadItem.RootNote(
                                    noteUiModel = feedItem
                                ).also { rootAdded = true }
                            } else {
                                ThreadItem.LeafNote(
                                    noteUiModel = feedItem,
                                    showConnector = !rootAdded,
                                )
                            }
                        }
                    }
                }
            }
        }

        return ThreadScreenUiState(
            title = stringManager[R.string.thread],
            pagingFlow = pagingFlow,
            getOpenGraphMetadata = getOpenGraphMetadata,
        ) { event ->
            when (event) {
                ThreadScreenUiEvent.OnBackClick -> navigator.pop()
                is ThreadScreenUiEvent.OnFeedEvent -> feedPresenterState.onEvent(event.feedUiEvent)
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(args: ThreadScreen, navigator: Navigator): ThreadScreenPresenter
    }
}
