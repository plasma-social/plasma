package social.plasma.ui.notifications

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.FeedContent
import social.plasma.ui.feed.NoteId
import social.plasma.ui.feed.NotificationsFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    onNavigateToThread: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToPostNote: () -> Unit,
    viewModel: NotificationsFeedViewModel = hiltViewModel(),
    onNavigateToReply: (NoteId) -> Unit,
) {

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            val userMetadata by viewModel.userMetadataState.collectAsState()

            RootScreenToolbar(
                title = stringResource(id = R.string.notifications),
                avatarUrl = userMetadata?.picture ?: " https://api.dicebear.com/5.x/bottts/jpg",
                onAvatarClick = { onNavigateToProfile(viewModel.pubkey) },
            )
        }) {
        val uiState by viewModel.uiState.collectAsState()

        FeedContent(
            modifier = Modifier.padding(it),
            uiState = uiState,
            onNavigateToProfile = onNavigateToProfile,
            onNoteDisposed = viewModel::onNoteDisposed,
            onNoteDisplayed = viewModel::onNoteDisplayed,
            onNoteClicked = onNavigateToThread,
            onAddNote = onNavigateToPostNote,
            onReactToNote = viewModel::onNoteReaction,
            onReply = onNavigateToReply,
        )
    }
}
