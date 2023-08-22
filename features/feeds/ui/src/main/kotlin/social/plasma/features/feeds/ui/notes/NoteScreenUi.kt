package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.LocalOverlayHost
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
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
        is NoteUiState.Loaded -> LoadedContent(state, modifier)

        NoteUiState.Loading -> LoadingCard(modifier = modifier)

        NoteUiState.NotFound -> NoteFoundCard(modifier)
    }
}

@Composable
private fun NoteFoundCard(modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f),
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        NoteNotFoundContent(
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun LoadedContent(
    state: NoteUiState.Loaded,
    modifier: Modifier,
) {
    val onEvent = state.onEvent
    val overlayHost = LocalOverlayHost.current
    val coroutineScope = rememberStableCoroutineScope()

    when (val style = state.style) {
        is NoteScreen.NoteStyle.ElevatedCard -> {
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
                })
        }

        is NoteScreen.NoteStyle.FlatCard -> {
            NoteFlatCard(
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
                })

            if (style.showLineBreak ) {
                Divider(modifier = Modifier.padding(horizontal = 8.dp))
                Spacer(Modifier.height(32.dp))
            }
        }

        is NoteScreen.NoteStyle.ThreadNote -> {
            ThreadNote(
                modifier = Modifier.clickable { onEvent(NoteUiEvent.OnClick) },
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
                showConnector = style.showConnector,
                onZapClick = {
                    coroutineScope.launch {
                        val zapAmount = overlayHost.getZapAmount()
                        onEvent(NoteUiEvent.OnZapClick(zapAmount))
                    }
                }
            )
        }
    }
}

