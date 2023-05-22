package social.plasma.onboarding.presenters

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginScreen
import javax.inject.Inject

class OnboardingPresentersFactory @Inject constructor(
    private val headlessAuthenticatorPresenter: HeadlessAuthenticatorPresenter.Factory,
    private val loginScreenPresenter: LoginPresenter.Factory,
    private val homePresenter: HomePresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is HeadlessAuthenticator -> headlessAuthenticatorPresenter.create(screen, navigator)
            LoginScreen -> loginScreenPresenter.create(navigator)
            HomeScreen -> homePresenter.create(navigator)
            else -> null
        }
    }
}
