package social.plasma.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import social.plasma.R
import social.plasma.ui.navigation.Navigation
import social.plasma.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var selectedNavItem by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (screen, icon) ->
                    NavigationBarItem(
                        selected = selectedNavItem == screen,
                        onClick = {
                            selectedNavItem = screen
                            navController.popBackStack()
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }

                                launchSingleTop = true

                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(icon), stringResource(screen.name)) },
                    )
                }
            }
        }) { paddingValues ->
        Navigation(navController, modifier = Modifier.padding(paddingValues))
    }
}

private val bottomNavItems = listOf(
    BottomNavRoute(Screen.Home, R.drawable.ic_plasma_feed),
    BottomNavRoute(Screen.Messages, R.drawable.ic_plasma_messages_outline),
    BottomNavRoute(Screen.Notifications, R.drawable.ic_plasma_notifications_outline),
)
