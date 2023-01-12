package social.plasma.ui.navigation

import androidx.annotation.StringRes
import social.plasma.R

sealed class Screen(val route: String, @StringRes val name: Int) {
    object Main : Screen(route = "main", name = R.string.main)
    object Home : Screen(route = "home", name = R.string.home)
    object Global : Screen(route = "global", name = R.string.global)
    object Notifications : Screen(route = "notifications", name = R.string.notifications)
}