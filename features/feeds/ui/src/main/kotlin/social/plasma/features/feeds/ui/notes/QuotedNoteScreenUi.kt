package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.feeds.screens.notes.QuotedNoteEvent
import social.plasma.features.feeds.screens.notes.QuotedNoteUiState

class QuotedNoteUi : Ui<QuotedNoteUiState> {
    @Composable
    override fun Content(state: QuotedNoteUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        EmbeddedNoteCard(
            uiModel = state.note,
            modifier = modifier.clickable {
                onEvent(QuotedNoteEvent.OnNoteClicked)
            },
            onAvatarClick = { onEvent(QuotedNoteEvent.OnAvatarClicked) },
            onNoteClick = { onEvent(QuotedNoteEvent.OnNoteClicked) },
        )
    }
}
