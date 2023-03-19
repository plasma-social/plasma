package social.plasma.profile.presenters

import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import social.plasma.features.profile.screens.ProfileScreen
import javax.inject.Inject

class ProfilePresentersFactory @Inject constructor(
    private val profileScreenPresenter: ProfileScreenPresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ProfileScreen -> profileScreenPresenter.create(screen, navigator)
            else -> null
        }
    }
}