package social.plasma.onboarding.presenters

import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginScreen
import javax.inject.Inject

class OnboardingPresentersFactory @Inject constructor(
    private val headlessAuthenticatorPresenter: HeadlessAuthenticatorPresenter.Factory,
    private val loginScreenPresenter: LoginPresenter.Factory,
    private val homePresenter: HomePresenter.Factory,
): Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when(screen) {
            HeadlessAuthenticator -> headlessAuthenticatorPresenter.create(navigator)
            LoginScreen -> loginScreenPresenter.create(navigator)
            HomeScreen -> homePresenter.create(navigator)
            else -> null
        }
    }
}