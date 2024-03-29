package social.plasma.onboarding.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.sentry.Sentry
import io.sentry.protocol.User
import social.plasma.domain.executeSync
import social.plasma.domain.interactors.AuthStatus
import social.plasma.domain.interactors.GetAuthStatus
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
            when (val status = getAuthStatus.executeSync()) {
                is AuthStatus.Authenticated,
                -> {
                    // Used to debug crashes in production
                    Sentry.setUser(User().apply {
                        id = status.pubkey.npub
                    })
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
