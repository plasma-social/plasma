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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import social.plasma.R
import social.plasma.ui.ThreadList
import social.plasma.ui.base.viewModelWithNavigator
import social.plasma.ui.home.HomeScreen
import social.plasma.ui.notifications.NotificationsScreen
import social.plasma.ui.post.Post
import social.plasma.ui.post.PostViewModel
import social.plasma.ui.profile.Profile
import social.plasma.ui.reply.ReplyViewModel

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
                onNavigateToProfile = { pubKey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubKey)
                    )
                },
                modifier = modifier,
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                navigateToPost = {
                    navHostController.navigate(Screen.PostNote.route)
                },
                onNavigateToReply = {
                    navHostController.navigate(Screen.Reply.buildRoute(it))
                }
            )
        }

        composable(Screen.Messages.route) {
            ComingSoon()
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateToProfile = { pubkey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubkey)
                    )
                },
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                modifier = modifier,
                onNavigateToPostNote = { navHostController.navigate(Screen.PostNote.route) },
                onNavigateToReply = { navHostController.navigate(Screen.Reply.buildRoute(it)) }
            )
        }

        composable(Screen.Profile.route) {
            Profile(
                modifier = modifier,
                onNavigateBack = { navHostController.popBackStack() },
                onNavigateToThread = {
                    navHostController.navigate(Screen.Thread.buildRoute(it))
                },
                onNavigateToReply = {
                    navHostController.navigate(Screen.Reply.buildRoute(it))
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
                },
                onNavigateToReply = {
                    navHostController.navigate(Screen.Reply.buildRoute(it))
                }
            )
        }

        composable(Screen.Reply.route) {
            val viewModel: ReplyViewModel = hiltViewModel()
            val state by viewModel.uiState().collectAsState()

            val title =
                // TODO move this to a better place and add the names of the note's p tags
                state.parentNote?.userMetadataEntity?.name?.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.replying_to_author, it)
                } ?: stringResource(id = R.string.create_reply)

            Post(
                state = state,
                onNoteChanged = viewModel::onNoteChange,
                title = title,
                onPostNote = { content ->
                    viewModel.onCreateReply(navHostController, content)
                },
                onBackClicked = { navHostController.popBackStack() }
            )
        }

        composable(Screen.PostNote.route) {
            val viewModel = viewModelWithNavigator<PostViewModel>(navigator = NavControllerNavigator(navHostController))
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
