package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityListItemScreen(val hashtag: String) : Screen
