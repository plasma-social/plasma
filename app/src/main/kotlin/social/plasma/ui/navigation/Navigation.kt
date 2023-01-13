package social.plasma.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import social.plasma.R
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

        composable(Screen.Messages.route) {
            ComingSoon()
        }

        composable(Screen.Notifications.route) {
            ComingSoon()
        }

        composable(Screen.Profile.route) {
            Profile()
        }
    }
}

@Composable
fun ComingSoon() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.coming_soon))
    }
}
