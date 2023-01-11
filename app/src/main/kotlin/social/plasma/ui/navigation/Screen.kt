package social.plasma.ui.navigation

import androidx.annotation.StringRes
import social.plasma.R

sealed class Screen(val route: String, @StringRes val name: Int) {
    object Main : Screen(route = "main", name = R.string.main)
    object Home : Screen(route = "home", name = R.string.home)
    object Messages : Screen(route = "messages", name = R.string.messages)
    object Search : Screen(route = "global", name = R.string.search)
    object Notifications : Screen(route = "notifications", name = R.string.notifications)
}