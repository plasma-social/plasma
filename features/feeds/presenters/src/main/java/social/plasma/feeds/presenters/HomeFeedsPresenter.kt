package social.plasma.feeds.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.onNavEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.SyncContactsEvents
import social.plasma.domain.observers.ObserveCurrentUserMetadata
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiEvent
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiState
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.shared.utils.api.StringManager

class HomeFeedsPresenter @AssistedInject constructor(
    private val syncContactsEvents: SyncContactsEvents,
    private val stringManager: StringManager,
    private val observeCurrentUserMetadata: ObserveCurrentUserMetadata,
    @Assisted private val navigator: Navigator,
) : Presenter<HomeFeedsUiState> {

    private val userMetadataFlow = observeCurrentUserMetadata.flow.onStart {
        observeCurrentUserMetadata(Unit)
    }

    @Composable
    override fun present(): HomeFeedsUiState {
        LaunchedEffect(Unit) {
            syncContactsEvents(Unit)
            syncContactsEvents.flow.collect()
        }

        val currentUserMetadata by remember { userMetadataFlow }.collectAsState(null)

        return HomeFeedsUiState(
            title = stringManager[R.string.feeds],
            toolbarAvatar = currentUserMetadata?.picture,
        ) { event ->
            when (event) {
                is HomeFeedsUiEvent.OnToolbarAvatarTapped -> {
                    currentUserMetadata?.pubkey?.let {
                        navigator.goTo(ProfileScreen(it))
                    }
                }

                is HomeFeedsUiEvent.ChildNav -> {
                    navigator.onNavEvent(event.navEvent)
                }
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomeFeedsPresenter
    }
}
