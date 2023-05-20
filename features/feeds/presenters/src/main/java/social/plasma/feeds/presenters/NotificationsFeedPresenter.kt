package social.plasma.feeds.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.observers.ObserveCurrentUserMetadata
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiEvent.ChildNav
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiEvent.OnToolbarAvatarTapped
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiState
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.shared.utils.api.StringManager

class NotificationsFeedPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    private val observeCurrentUserMetadata: ObserveCurrentUserMetadata,
    @Assisted private val navigator: Navigator,
) : Presenter<NotificationsFeedUiState> {

    private val userMetadataFlow = observeCurrentUserMetadata.flow.onStart {
        observeCurrentUserMetadata(Unit)
    }

    @Composable
    override fun present(): NotificationsFeedUiState {
        val currentUserMetadata by remember { userMetadataFlow }.collectAsState(null)

        return NotificationsFeedUiState(
            title = stringManager[R.string.notifications],
            toolbarAvatar = currentUserMetadata?.picture,
        ) { event ->
            when (event) {
                is OnToolbarAvatarTapped -> {
                    currentUserMetadata?.pubkey?.let {
                        navigator.goTo(ProfileScreen(it))
                    }
                }

                is ChildNav -> {
                    navigator.onNavEvent(event.navEvent)
                }
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): NotificationsFeedPresenter
    }
}
