package social.plasma.features.feeds.screens.threads

import com.slack.circuit.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class HashTagFeedScreen(
    val hashTag: String,
) : Screen
