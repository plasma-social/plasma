package social.plasma.ui.feed

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import social.plasma.models.Note
import social.plasma.repository.NoteRepository
import social.plasma.ui.components.FeedCardUiModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
) : ViewModel() {
    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val uiState: StateFlow<FeedUiState> = moleculeScope.launchMolecule(recompositionClock) {
        val noteList by remember { noteRepository.observeNotes() }.collectAsState(initial = null)

        noteList?.let { notes ->
            val feedCardList = notes.map { it.toFeedUiModel() }
            FeedUiState.Loaded(feedCardList)
        } ?: FeedUiState.Loading
    }
}

private fun Note.toFeedUiModel(): FeedCardUiModel = FeedCardUiModel(
    id = id,
    name = "${pubKey.take(8)}...${pubKey.takeLast(8)}",
    nip5 = "nostrplebs.com",
    content = content,
    timePosted = "1m",
    imageUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=${pubKey}",
    likeCount = "1.2k",
    shareCount = "13",
    replyCount = "50"
)
