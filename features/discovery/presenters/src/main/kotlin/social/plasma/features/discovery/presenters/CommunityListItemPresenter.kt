package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.discovery.screens.communities.CommunityListItemEvent
import social.plasma.features.discovery.screens.communities.CommunityListItemScreen
import social.plasma.features.discovery.screens.communities.CommunityListItemUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen

class CommunityListItemPresenter @AssistedInject constructor(
    @Assisted private val args: CommunityListItemScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<CommunityListItemUiState> {

    @Composable
    override fun present(): CommunityListItemUiState {

        return CommunityListItemUiState(args.hashtag) { event ->
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
