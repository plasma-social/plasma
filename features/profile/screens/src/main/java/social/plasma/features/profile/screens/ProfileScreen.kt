package social.plasma.features.profile.screens

import kotlinx.parcelize.Parcelize
import social.plasma.common.screens.StandaloneScreen

@Parcelize
data class ProfileScreen(
    val pubKeyHex: String,
) : StandaloneScreen
