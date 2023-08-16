package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteEvent
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteUiState
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteUiState.Loading
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteUiState.NoteNotFound

class QuotedNoteUi : Ui<QuotedNoteUiState> {
    @Composable
    override fun Content(state: QuotedNoteUiState, modifier: Modifier) {
        when (state) {
            is QuotedNoteUiState.Loaded -> LoadedCard(state, modifier)
            Loading -> LoadingCard(modifier)
            NoteNotFound -> NotFoundCard(modifier)
        }
    }

    @Composable
    private fun LoadedCard(
        state: QuotedNoteUiState.Loaded,
        modifier: Modifier,
    ) {
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

    @Composable
    private fun NotFoundCard(modifier: Modifier) {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f),
        ) {
            NoteNotFoundContent(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    @Composable
    private fun LoadingCard(modifier: Modifier) {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .border(CardDefaults.outlinedCardBorder(), CardDefaults.outlinedShape)
                .placeholder(
                    visible = true,
                    color = MaterialTheme.colorScheme.surface,
                    highlight = PlaceholderHighlight.shimmer(MaterialTheme.colorScheme.background),
                    shape = CardDefaults.outlinedShape,
                ),
            content = {},
        )
    }
}
