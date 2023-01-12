package social.plasma.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import social.plasma.ui.main.MainScreen

@Composable
fun PlasmaApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    //TODO add login screen
    MainScreen(
        modifier = modifier,
        navController = navController,
    )
}
