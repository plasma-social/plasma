package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.EventCountInteractor
import social.plasma.domain.interactors.GetHashTagFollowerCount
import social.plasma.domain.observers.ObserveCommunityAvatars
import social.plasma.domain.observers.ObserveHashTagNewNoteCount
import social.plasma.features.discovery.screens.communities.CommunityListItemEvent
import social.plasma.features.discovery.screens.communities.CommunityListItemScreen
import social.plasma.features.discovery.screens.communities.CommunityListItemUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.shared.utils.api.NumberFormatter
import social.plasma.shared.utils.api.StringManager

class CommunityListItemPresenter @AssistedInject constructor(
    private val getHashTagFollowerCount: GetHashTagFollowerCount,
    private val observeHashTagNewNoteCount: ObserveHashTagNewNoteCount,
    private val observeCommunityAvatars: ObserveCommunityAvatars,
    private val numberFormatter: NumberFormatter,
    private val stringManager: StringManager,
    @Assisted private val args: CommunityListItemScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<CommunityListItemUiState> {

    private val captionTextFlow = observeHashTagNewNoteCount.flow.onStart {
        observeHashTagNewNoteCount(ObserveHashTagNewNoteCount.Params(args.hashtag))
    }.map { count ->
        stringManager.getFormattedString(
            R.string.hashtag_note_count, mapOf(
                "count" to count,
                "formattedCount" to numberFormatter.format(count),
            )
        )
    }

    @Composable
    override fun present(): CommunityListItemUiState {
        val trailingText by produceState(initialValue = "") {
            val result =
                getHashTagFollowerCount.executeSync(GetHashTagFollowerCount.Params(args.hashtag))

            value = when (result) {
                is EventCountInteractor.Result.Success -> {
                    stringManager.getFormattedString(
                        R.string.hashtag_follower_count, mapOf(
                            "count" to result.count,
                            "formattedCount" to numberFormatter.format(result.count),
                        )
                    )
                }

                EventCountInteractor.Result.Failure -> ""
            }
        }

        val captionText by remember { captionTextFlow }.collectAsState(initial = "")
        val avatarList by remember {
            observeCommunityAvatars.flow.onStart {
                observeCommunityAvatars(ObserveCommunityAvatars.Params(limit = 6, args.hashtag))
            }
        }.collectAsState(initial = emptyList())

        return CommunityListItemUiState(
            name = args.hashtag.displayName,
            trailingText = trailingText,
            avatarList = avatarList,
            captionText = captionText,
        ) { event ->
            when (event) {
                is CommunityListItemEvent.OnClick -> navigator.goTo(HashTagFeedScreen(args.hashtag))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: CommunityListItemScreen, navigator: Navigator): CommunityListItemPresenter
    }
}
