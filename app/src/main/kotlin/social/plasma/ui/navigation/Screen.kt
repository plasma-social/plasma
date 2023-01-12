package social.plasma.ui.navigation

import androidx.annotation.StringRes
import social.plasma.R
import social.plasma.models.PubKey

sealed class Screen(
    val route: String,
    @StringRes val name: Int,
) {
    object Profile : Screen(route = "profile/{pubkey}", name = R.string.profile) {
        fun buildRoute(pubKey: PubKey): String = route.replace("{pubkey}", pubKey.value)
    }

    object Home : Screen(route = "home", name = R.string.home)

    object Global : Screen(route = "global", name = R.string.global)

    object Notifications : Screen(
        route = "notifications",
        name = R.string.notifications,
    )
}
