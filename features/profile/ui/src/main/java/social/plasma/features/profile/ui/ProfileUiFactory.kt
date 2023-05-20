package social.plasma.features.profile.ui

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.profile.screens.ProfileScreen
import javax.inject.Inject

class ProfileUiFactory @Inject constructor(
    private val profileScreenUi: ProfileScreenUi,
) : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is ProfileScreen -> profileScreenUi
            else -> null
        }
    }
}
