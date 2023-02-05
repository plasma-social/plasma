package social.plasma.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import social.plasma.R
import social.plasma.ui.ThreadList
import social.plasma.ui.home.HomeScreen
import social.plasma.ui.notifications.NotificationsScreen
import social.plasma.ui.post.Post
import social.plasma.ui.post.postViewModel
import social.plasma.ui.profile.Profile

@Composable
fun Navigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                modifier = modifier,
                onNavigateToProfile = { pubKey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubKey)
                    )
                },
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                navigateToPost = {
                    navHostController.navigate(Screen.PostNote.route)
                },
            )
        }

        composable(Screen.Messages.route) {
            ComingSoon()
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                modifier = modifier,
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                onNavigateToProfile = { pubkey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubkey)
                    )
                },
                onNavigateToPostNote = { navHostController.navigate(Screen.PostNote.route) }
            )
        }

        composable(Screen.Profile.route) {
            Profile(
                modifier = modifier,
                onNavigateBack = { navHostController.popBackStack() },
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                }
            )
        }

        composable(Screen.Thread.route) {
            ThreadList(
                modifier = modifier,
                onNavigateBack = { navHostController.popBackStack() },
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                onNavigateToProfile = { pubkey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubkey)
                    )
                }
            )
        }

        composable(Screen.PostNote.route) {
            val viewModel = postViewModel(navigator = NavControllerNavigator(navHostController))
            val state by viewModel.uiState().collectAsState()
            Post(
                state = state,
                onNoteChanged = viewModel::onNoteChange,
                onPostNote = viewModel::onPostNote,
                onBackClicked = { navHostController.popBackStack() }
            )
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
