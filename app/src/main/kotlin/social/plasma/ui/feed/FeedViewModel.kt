package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import social.plasma.models.Note
import social.plasma.models.PubKey
import social.plasma.models.TypedEvent
import social.plasma.repository.NoteRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.components.NoteCardUiModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
) : MoleculeViewModel<FeedUiState>(recompositionClock) {

    private val noteObserver = noteRepository.observeGlobalNotes().map { noteList ->
        noteList.map { it.toFeedUiModel() }
    }

    @Composable
    override fun models(): FeedUiState {
        val noteList by remember { noteObserver }.collectAsState(initial = null)

        return noteList?.let { FeedUiState.Loaded(it) } ?: FeedUiState.Loading
    }
}

private fun TypedEvent<Note>.toFeedUiModel(): NoteCardUiModel {
    val pubKeyHex = pubKey.hex()
    return NoteCardUiModel(
        id = id.hex(),
        name = "${pubKeyHex.take(8)}...${pubKeyHex.drop(48)}",
        nip5 = "nostrplebs.com",
        content = content.text,
        timePosted = "1m",
        avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=$pubKeyHex",
        likeCount = "1.2k",
        shareCount = "13",
        replyCount = "50",
        userPubkey = PubKey(pubKeyHex)
    )
}
