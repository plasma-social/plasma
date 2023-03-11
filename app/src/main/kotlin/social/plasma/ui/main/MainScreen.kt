package social.plasma.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import social.plasma.R
import social.plasma.ui.components.HorizontalSeparator
import social.plasma.ui.navigation.Navigation
import social.plasma.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var bottomNavigationVisible by remember { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    bottomNavigationVisible = when (navBackStackEntry?.destination?.route) {
        Screen.Profile.route,
        Screen.PostNote.route,
        -> false

        else -> true
    }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(visible = bottomNavigationVisible) {
                BottomNavigationBar(navController)
            }
        }) { paddingValues ->
        Navigation(navController, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
) {
    var selectedNavItem by rememberSaveable { mutableStateOf(0) }
    Column {
        HorizontalSeparator()
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            bottomNavItems.forEachIndexed { index, (screen, icon) ->
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surface,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                    selected = selectedNavItem == index,
                    onClick = {
                        selectedNavItem = index
                        navController.popBackStack()
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    },
                    label = { Text(stringResource(screen.name)) },
                    icon = { Icon(painterResource(icon), stringResource(screen.name)) },
                )
            }
        }
    }

}

private val bottomNavItems = listOf(
    BottomNavRoute(Screen.Home, R.drawable.ic_plasma_feed),
    BottomNavRoute(Screen.Messages, R.drawable.ic_plasma_messages_outline),
    BottomNavRoute(Screen.Notifications, R.drawable.ic_plasma_notifications_outline),
)
