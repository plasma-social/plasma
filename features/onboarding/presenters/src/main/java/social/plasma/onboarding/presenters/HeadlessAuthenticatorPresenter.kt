package social.plasma.onboarding.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.slack.circuit.CircuitUiState
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.domain.interactors.AuthStatus
import social.plasma.domain.interactors.GetAuthStatus
import social.plasma.domain.executeSync
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginScreen

class HeadlessAuthenticatorPresenter @AssistedInject constructor(
    private val getAuthStatus: GetAuthStatus,
    @Assisted private val args: HeadlessAuthenticator,
    @Assisted private val navigator: Navigator,
) : Presenter<CircuitUiState> {
    @Composable
    override fun present(): CircuitUiState {
        LaunchedEffect(Unit) {
            when (getAuthStatus.executeSync()) {
                is AuthStatus.ReadOnly,
                is AuthStatus.Authenticated,
                -> {
                    navigator.resetRoot(HomeScreen)
                    args.exitScreen?.let(navigator::goTo)
                }

                else -> navigator.resetRoot(LoginScreen)
            }
        }

        return object : CircuitUiState {}
    }

    @AssistedFactory
    interface Factory {
        fun create(
            args: HeadlessAuthenticator,
            navigator: Navigator,
        ): HeadlessAuthenticatorPresenter
    }
}
