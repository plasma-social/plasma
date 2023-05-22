package social.plasma.features.discovery.presenters

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import social.plasma.features.discovery.screens.relaylist.RelayListScreen
import social.plasma.features.discovery.screens.search.SearchScreen
import javax.inject.Inject

class DiscoveryPresentersFactory @Inject constructor(
    private val searchScreenPresenter: SearchScreenPresenter.Factory,
    private val relayListPresenter: RelayListPresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            SearchScreen -> searchScreenPresenter.create(navigator, forceActive = true)
            RelayListScreen -> relayListPresenter.create(navigator)
            else -> null
        }
    }
}
