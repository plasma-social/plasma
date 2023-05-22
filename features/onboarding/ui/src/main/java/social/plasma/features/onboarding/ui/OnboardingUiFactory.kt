package social.plasma.features.onboarding.ui

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginScreen
import social.plasma.features.onboarding.ui.home.HomeScreenUi
import social.plasma.features.onboarding.ui.login.LoginScreenUi
import javax.inject.Inject

class OnboardingUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is HeadlessAuthenticator -> HeadlessScreen()
            is LoginScreen -> LoginScreenUi()
            is HomeScreen -> HomeScreenUi()
            else -> null
        }
    }
}

