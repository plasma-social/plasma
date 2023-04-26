package social.plasma.features.search.ui

import com.slack.circuit.CircuitContext
import com.slack.circuit.Screen
import com.slack.circuit.Ui
import social.plasma.features.search.screens.SearchScreen
import javax.inject.Inject

class SearchUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is SearchScreen -> SearchScreenUi()
            else -> null
        }
    }
}

