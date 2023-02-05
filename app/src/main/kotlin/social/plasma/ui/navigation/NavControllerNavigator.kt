package social.plasma.ui.navigation

import androidx.navigation.NavController

class NavControllerNavigator(
    private val navController: NavController,
) : Navigator {
    override fun goToRoute(route: String) {
        navController.navigate(route)
    }

    override fun goBack() {
        navController.popBackStack()
    }
}