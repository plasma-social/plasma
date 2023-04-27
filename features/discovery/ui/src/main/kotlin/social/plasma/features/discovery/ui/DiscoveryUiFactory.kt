package social.plasma.features.discovery.ui

import com.slack.circuit.CircuitContext
import com.slack.circuit.Screen
import com.slack.circuit.Ui
import social.plasma.features.discovery.screens.search.SearchScreen
import social.plasma.features.discovery.ui.search.SearchScreenUi
import javax.inject.Inject

class DiscoveryUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is SearchScreen -> SearchScreenUi()
            else -> null
        }
    }
}
