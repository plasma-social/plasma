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
import social.plasma.ui.post.PostUiEvent.OnNoteChange
import social.plasma.ui.post.PostUiEvent.PostNote
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
                onNavigateToThread = { noteId ->
                    navHostController.navigate(Screen.Thread.buildRoute(noteId.hex))
                },
                navigateToPost = {
                    navHostController.navigate(Screen.PostNote.route)
                },
                onNavigateToReply = { noteId ->
                    navHostController.navigate(Screen.Reply.buildRoute(noteId.hex))
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
                onNavigateToThread = { noteId ->
                    navHostController.navigate(Screen.Thread.buildRoute(noteId.hex))
                },
                modifier = modifier,
                onNavigateToPostNote = { navHostController.navigate(Screen.PostNote.route) },
                onNavigateToReply = { noteId ->
                    navHostController.navigate(
                        Screen.Reply.buildRoute(
                            noteId.hex
                        )
                    )
                }
            )
        }

        composable(Screen.Profile.route) {
            Profile(
                modifier = modifier,
                onNavigateBack = { navHostController.popBackStack() },
                onNavigateToThread = { noteId ->
                    navHostController.navigate(Screen.Thread.buildRoute(noteId.hex))
                },
                onNavigateToReply = { noteId ->
                    navHostController.navigate(Screen.Reply.buildRoute(noteId.hex))
                },
                onNavigateToProfile = {
                    navHostController.navigate(
                        Screen.Profile.buildRoute(it)
                    )
                }
            )
        }

        composable(Screen.Thread.route) {
            ThreadList(
                modifier = modifier,
                onNavigateBack = { navHostController.popBackStack() },
                onNavigateToThread = { noteId ->
                    navHostController.navigate(Screen.Thread.buildRoute(noteId.hex))
                },
                onNavigateToProfile = { pubkey ->
                    navHostController.navigate(
                        Screen.Profile.buildRoute(pubkey)
                    )
                },
                onNavigateToReply = { noteId ->
                    navHostController.navigate(Screen.Reply.buildRoute(noteId.hex))
                }
            )
        }

        composable(Screen.Reply.route) {
            val viewModel: ReplyViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            val title =
                // TODO move this to a better place and add the names of the note's p tags
                state.parentNote?.userMetadataEntity?.name?.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.replying_to_author, it)
                } ?: stringResource(id = R.string.create_reply)

            Post(
                state = state,
                onNoteChanged = { viewModel.onEvent(OnNoteChange(it)) },
                title = title,
                onPostNote = {
                    viewModel.onEvent(PostNote)
                    // TODO pass to viewmodel
                    navHostController.popBackStack()
                },
                onBackClicked = { navHostController.popBackStack() }
            )
        }

        composable(Screen.PostNote.route) {
            val viewModel = viewModelWithNavigator<PostViewModel>(
                navigator = NavControllerNavigator(navHostController)
            )
            val state by viewModel.uiState.collectAsState()
            Post(
                state = state,
                onNoteChanged = { viewModel.onEvent(OnNoteChange(it)) },
                onPostNote = { viewModel.onEvent(PostNote) },
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
