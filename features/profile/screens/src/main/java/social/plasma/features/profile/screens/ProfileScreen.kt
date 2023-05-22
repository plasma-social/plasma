package social.plasma.features.profile.screens

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileScreen(
    val pubKeyHex: String,
) : Screen
