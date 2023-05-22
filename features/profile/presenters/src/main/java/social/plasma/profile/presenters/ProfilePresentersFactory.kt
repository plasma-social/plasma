package social.plasma.profile.presenters

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
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
