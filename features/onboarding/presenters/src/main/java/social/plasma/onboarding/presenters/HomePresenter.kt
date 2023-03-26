package social.plasma.onboarding.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.onNavEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.SyncAllEvents
import social.plasma.domain.interactors.SyncProfileData
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.onboarding.screens.home.HomeUiEvent
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnFabClick
import social.plasma.features.onboarding.screens.home.HomeUiState
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.models.PubKey
import social.plasma.shared.repositories.api.AccountStateRepository

class HomePresenter @AssistedInject constructor(
    private val syncAllEvents: SyncAllEvents,
    private val syncProfileData: SyncProfileData,
    private val observeMyMetadata: ObserveUserMetadata,
    accountStateRepository: AccountStateRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<HomeUiState> {
    private val pubKey = PubKey.of(accountStateRepository.getPublicKey()!!)
    private val metadataFlow = observeMyMetadata.flow

    @Composable
    override fun present(): HomeUiState {
        LaunchedEffect(Unit) {
            launch { observeMyMetadata(ObserveUserMetadata.Params(pubKey)) }
            launch { syncAllEvents.executeSync(SyncAllEvents.Params(pubKey)) }
            launch { syncProfileData.executeSync(SyncProfileData.Params(pubKey)) }
        }

        val metadata by metadataFlow.collectAsState(initial = null)

        return HomeUiState(avatarUrl = metadata?.picture) { event ->
            when (event) {
                OnFabClick -> navigator.goTo(ComposingScreen())
                HomeUiEvent.OnAvatarClick -> navigator.goTo(ProfileScreen(pubKeyHex = pubKey.hex))
                is HomeUiEvent.OnChildNav -> navigator.onNavEvent(event.navEvent)
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
