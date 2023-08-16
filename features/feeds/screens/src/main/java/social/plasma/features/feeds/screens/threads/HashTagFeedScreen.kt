package social.plasma.features.feeds.screens.threads

import kotlinx.parcelize.Parcelize
import social.plasma.common.screens.StandaloneScreen
import social.plasma.models.HashTag

@Parcelize
data class HashTagFeedScreen(
    val hashTag: HashTag,
) : StandaloneScreen
