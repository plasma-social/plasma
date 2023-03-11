package social.plasma.ui.profilescreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.profile.Profile
import social.plasma.ui.profile.ProfileUiState

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToThread: (NoteId) -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by profileViewModel.uiState.collectAsState(ProfileUiState.Loading)

    Profile(
        uiState = uiState,
        modifier = modifier,
        onNoteDisposed = profileViewModel::onNoteDisposed,
        onNoteDisplayed = profileViewModel::onNoteDisplayed,
        onNavigateBack = onNavigateBack,
        onNoteClick = onNavigateToThread,
        onNoteReaction = profileViewModel::onNoteReaction,
        onReply = onNavigateToReply,
        getOpenGraphMetadata = profileViewModel::getOpenGraphMetadata,
        onNavigateToProfile = onNavigateToProfile,
        onRepostClick = profileViewModel::onRepostClick,
    )
}

