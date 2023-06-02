package social.plasma.feeds.presenters.hashtag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.FollowHashTag
import social.plasma.domain.interactors.SyncHashTagEvents
import social.plasma.domain.interactors.UnfollowHashTag
import social.plasma.domain.observers.ObserveFollowedHashTags
import social.plasma.domain.observers.ObservePagedHashTagFeed
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.features.feeds.screens.hashtags.ButtonUiState
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.feeds.presenters.feed.FeedUiProducer
import social.plasma.shared.utils.api.StringManager

class HashTagScreenPresenter @AssistedInject constructor(
    private val feedUiProducer: FeedUiProducer,
    observePagedHashTagFeed: ObservePagedHashTagFeed,
    observeFollowedHashTags: ObserveFollowedHashTags,
    private val followHashTag: FollowHashTag,
    private val unfollowHashTag: UnfollowHashTag,
    private val syncHashTagEvents: SyncHashTagEvents,
    private val stringManager: StringManager,
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

    private val hashtagFollowState = observeFollowedHashTags.flow.map { followedHashTags ->
        val hashtag = if (args.hashTag.startsWith("#")) args.hashTag.substring(1) else args.hashTag
        followedHashTags.contains(hashtag)
    }.onStart {
        observeFollowedHashTags(Unit)
    }

    @Composable
    override fun present(): HashTagScreenUiState {
        LaunchedEffect(Unit) {
            syncHashTagEvents.executeSync(SyncHashTagEvents.Params(args.hashTag))
        }

        val feedState = feedUiProducer(navigator, pagingFlow)

        var optimisticFollowState: Boolean? by remember { mutableStateOf(null) }

        val currentFollowState by remember { hashtagFollowState }.collectAsState(initial = false)

        val followingHashTag = optimisticFollowState ?: currentFollowState

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

        var followButtonEnabled by remember { mutableStateOf(true) }

        val followButtonUiState = ButtonUiState(
            enabled = followButtonEnabled,
            style = if (followingHashTag) ButtonUiState.Style.PrimaryOutline else ButtonUiState.Style.Primary,
            label = if (followingHashTag) stringManager[R.string.leave] else stringManager[R.string.join],
        )

        val coroutineScope = rememberCoroutineScope()
        return HashTagScreenUiState(
            title = args.hashTag,
            feedState = hashTagScreenFeedState,
            followButtonUiState = followButtonUiState,
        ) { event ->
            when (event) {
                HashTagScreenUiEvent.OnNavigateBack -> navigator.pop()
                HashTagScreenUiEvent.OnFollowButtonClick -> {
                    coroutineScope.launch {
                        followButtonEnabled = false
                        optimisticFollowState = !followingHashTag
                        if (followingHashTag) {
                            unfollowHashTag.executeSync(UnfollowHashTag.Params(args.hashTag))
                        } else {
                            followHashTag.executeSync(FollowHashTag.Params(args.hashTag))
                        }
                        followButtonEnabled = true
                    }
                }
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(args: HashTagFeedScreen, navigator: Navigator): HashTagScreenPresenter
    }
}
