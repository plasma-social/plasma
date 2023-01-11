package social.plasma.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy

fun NavBackStackEntry.isActiveTab(screen: Screen): Boolean =
    destination.hierarchy.any { it.route == screen.route }