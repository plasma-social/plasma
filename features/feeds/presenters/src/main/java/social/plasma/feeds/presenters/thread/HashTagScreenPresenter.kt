package social.plasma.feeds.presenters.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.paging.PagingConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.SyncHashTagEvents
import social.plasma.domain.observers.ObservePagedHashTagFeed
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.feeds.presenters.feed.FeedPresenter
import social.plasma.feeds.presenters.feed.NotePagingFlowMapper
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

class HashTagScreenPresenter @AssistedInject constructor(
    feedPresenterFactory: FeedPresenter.Factory,
    observePagedHashTagFeed: ObservePagedHashTagFeed,
    private val syncHashTagEvents: SyncHashTagEvents,
    notePagingFlowMapper: NotePagingFlowMapper,
    private val stringManager: StringManager,
    private val openGraphParser: OpenGraphParser,
    @Assisted private val args: HashTagFeedScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<HashTagScreenUiState> {
    private val pagingFlow = observePagedHashTagFeed.flow.onStart {
        observePagedHashTagFeed(
            ObservePagedHashTagFeed.Params(
                hashTag = args.hashTag,
                pagingConfig = PagingConfig(
                    pageSize = 20,
                )
            )
        )
    }

    private val feedPresenter =
        feedPresenterFactory.create(navigator, notePagingFlowMapper.map(pagingFlow))
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
    override fun present(): HashTagScreenUiState {
        LaunchedEffect(Unit) {
            syncHashTagEvents.executeSync(SyncHashTagEvents.Params(args.hashTag))
        }

        val feedState = feedPresenter.present()

        val hashTagScreenFeedState = remember(feedState) {
            val onFeedEvent = feedState.onEvent
            feedState.copy(
                onEvent = { event ->
                    when (event) {
                        // Prevents navigating to the same hashtag screen
                        is FeedUiEvent.OnHashTagClick -> {
                            if (event.hashTag.lowercase() != args.hashTag.lowercase()) {
                                onFeedEvent(event)
                            }
                        }

                        else -> onFeedEvent(event)
                    }
                }
            )
        }

        return HashTagScreenUiState(
            title = args.hashTag,
            pagingFlow = feedState.pagingFlow,
            getOpenGraphMetadata = getOpenGraphMetadata,
            feedState = hashTagScreenFeedState,
        ) { event ->
            when (event) {
                HashTagScreenUiEvent.OnNavigateBack -> navigator.pop()
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(args: HashTagFeedScreen, navigator: Navigator): HashTagScreenPresenter
    }
}
