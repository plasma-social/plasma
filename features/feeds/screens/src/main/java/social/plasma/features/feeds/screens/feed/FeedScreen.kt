package social.plasma.features.feeds.screens.feed

import com.slack.circuit.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedScreen(
    val feedType: FeedType,
) : Screen

enum class FeedType {
    Following,
    Replies,
    Notifications,
}
