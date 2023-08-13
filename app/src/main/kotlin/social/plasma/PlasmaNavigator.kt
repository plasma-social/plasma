package social.plasma

import android.content.Intent
import android.net.Uri
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.screen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import social.plasma.common.screens.AndroidScreens

class PlasmaNavigator(
    private val circuitNavigator: Navigator,
    private val backstack: SaveableBackStack,
    val openIntent: (Intent) -> Unit,
) : Navigator {
    private val currentScreen get() = backstack.topRecord?.screen

    override fun goTo(screen: Screen) {
        when (screen) {
            is AndroidScreens.ShareLightningInvoiceScreen -> {
                try {
                    openIntent(Intent(Intent.ACTION_VIEW, Uri.parse("lightning:${screen.invoice}")))
                } catch (activityNotFoundException: Exception) {
                    // TODO show wallet-required dialog
                }
            }

            else -> {
                if (screen == currentScreen) {
                    return
                }

                circuitNavigator.goTo(screen)
            }
        }
    }

    override fun pop(): Screen? {
        return circuitNavigator.pop()
    }

    override fun resetRoot(newRoot: Screen): List<Screen> {
        return circuitNavigator.resetRoot(newRoot)
    }
}
