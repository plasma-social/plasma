package social.plasma.ui.notifications

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.R
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.FeedContent
import social.plasma.ui.feed.NotificationsFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    onNavigateToThread: (NoteId) -> Unit,
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
                avatarUrl = userMetadata?.picture,
                onAvatarClick = { onNavigateToProfile(viewModel.pubkey) },
            )
        }) {
        val uiState by viewModel.uiState.collectAsState()

        FeedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            uiState = uiState,
            onNavigateToProfile = onNavigateToProfile,
            onNoteClicked = onNavigateToThread,
            onAddNote = onNavigateToPostNote,
            onReply = onNavigateToReply,
            getOpenGraphMetadata = viewModel::getOpenGraphMetadata,
            onEvent = viewModel::onEvent
        )
    }
}
