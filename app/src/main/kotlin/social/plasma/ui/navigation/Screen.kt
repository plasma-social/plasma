package social.plasma.ui.navigation

import androidx.annotation.StringRes
import social.plasma.PubKey
import social.plasma.R

sealed class Screen(
    val route: String,
    @StringRes val name: Int,
) {
    object Profile : Screen(route = "profile/{pubkey}", name = R.string.profile) {
        fun buildRoute(pubKey: PubKey): String = route.replace("{pubkey}", pubKey.hex)
    }

    object Home : Screen(route = "home", name = R.string.home)

    object Messages : Screen(route = "messages", name = R.string.messages)

    object Notifications : Screen(
        route = "notifications",
        name = R.string.notifications,
    )
}
