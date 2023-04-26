package social.plasma.features.search.presenters

import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import social.plasma.features.search.screens.SearchScreen
import javax.inject.Inject

class SearchPresentersFactory @Inject constructor(
    private val searchScreenPresenter: SearchScreenPresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            SearchScreen -> searchScreenPresenter.create(navigator)
            else -> null
        }
    }
}
