package social.plasma.features.feeds.screens.threads

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.HashTag

@Parcelize
data class HashTagFeedScreen(
    val hashTag: HashTag,
) : Screen
