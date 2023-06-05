package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.HashTag

@Parcelize
data class CommunityListItemScreen(val hashtag: HashTag) : Screen
