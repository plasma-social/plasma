package social.plasma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import social.plasma.ui.feed.Feed
import social.plasma.ui.home.HomeScreen
import social.plasma.ui.profile.Profile

@Composable
fun Navigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navHostController,
        modifier = modifier,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigateToProfile = { pubKey ->
                navHostController.navigate(
                    Screen.Profile.buildRoute(pubKey)
                )
            })
        }

        composable(Screen.Global.route) {
            Feed(onNavigateToProfile = { pubKey ->
                navHostController.navigate(
                    Screen.Profile.buildRoute(pubKey)
                )
            })
        }

        composable(Screen.Notifications.route) {
            Feed(onNavigateToProfile = { pubKey ->
                navHostController.navigate(
                    Screen.Profile.buildRoute(pubKey)
                )
            })
        }

        composable(Screen.Profile.route) {
            Profile()
        }
    }
}
