package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.overlay.LocalOverlayHost
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiEvent
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiState
import social.plasma.features.feeds.ui.LoadingCard
import social.plasma.ui.overlays.getZapAmount
import social.plasma.ui.rememberStableCoroutineScope

@Composable
fun NoteScreenUi(
    state: NoteUiState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is NoteUiState.Loaded -> {
            val onEvent = state.onEvent
            val overlayHost = LocalOverlayHost.current
            val coroutineScope = rememberStableCoroutineScope()

            NoteElevatedCard(
                modifier = modifier.clickable { onEvent(NoteUiEvent.OnClick) },
                uiModel = state.noteCard,
                onAvatarClick = { onEvent(NoteUiEvent.OnAvatarClick) },
                onLikeClick = { onEvent(NoteUiEvent.OnLikeClick) },
                onReplyClick = { onEvent(NoteUiEvent.OnReplyClick) },
                onProfileClick = { onEvent(NoteUiEvent.OnProfileClick(it)) },
                onNoteClick = { onEvent(NoteUiEvent.OnNoteClick(it)) },
                onRepostClick = { onEvent(NoteUiEvent.OnRepostClick) },
                getOpenGraphMetadata = { null },
                onHashTagClick = { onEvent(NoteUiEvent.OnHashTagClick(it)) },
                onNestedNavEvent = { onEvent(NoteUiEvent.OnNestedNavEvent(it)) },
                onZapClick = {
                    coroutineScope.launch {
                        val zapAmount = overlayHost.getZapAmount()
                        onEvent(NoteUiEvent.OnZapClick(zapAmount))
                    }
                }
            )
        }

        NoteUiState.Loading -> {
            LoadingCard(modifier = modifier)
        }
    }
}
