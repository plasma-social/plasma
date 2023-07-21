package social.plasma.common.screens

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize

/**
 * Screens that interact with the Android system.
 * Used for intents, deeplinks, etc.
 */
sealed interface AndroidScreens : Screen {
    @Parcelize
    data class ShareLightningInvoiceScreen(val invoice: String) : AndroidScreens
}
