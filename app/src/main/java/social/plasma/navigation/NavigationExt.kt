package social.plasma.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy

fun NavBackStackEntry.isActiveScreen(screen: Screen): Boolean =
    destination.hierarchy.any { it.route == screen.route }