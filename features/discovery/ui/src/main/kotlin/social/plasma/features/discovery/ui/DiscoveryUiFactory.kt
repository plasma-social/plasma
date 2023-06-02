package social.plasma.features.discovery.ui

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.communities.CommunityListItemScreen
import social.plasma.features.discovery.screens.communities.CommunityListScreen
import social.plasma.features.discovery.screens.relaylist.RelayListScreen
import social.plasma.features.discovery.screens.search.SearchScreen
import social.plasma.features.discovery.ui.community.CommunityListItemUi
import social.plasma.features.discovery.ui.community.CommunityListUi
import social.plasma.features.discovery.ui.relays.RelayListScreenUi
import social.plasma.features.discovery.ui.search.SearchScreenUi
import javax.inject.Inject

class DiscoveryUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is SearchScreen -> SearchScreenUi()
            is RelayListScreen -> RelayListScreenUi()
            is CommunityListScreen -> CommunityListUi()
            is CommunityListItemScreen -> CommunityListItemUi()
            else -> null
        }
    }
}
