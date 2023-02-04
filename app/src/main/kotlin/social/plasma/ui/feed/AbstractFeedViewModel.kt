package social.plasma.ui.feed

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.cash.molecule.RecompositionClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import social.plasma.PubKey
import social.plasma.db.notes.NoteWithUser
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.base.MoleculeViewModel
import social.plasma.ui.mappers.NotePagingFlowMapper

abstract class AbstractFeedViewModel(
    recompositionClock: RecompositionClock,
    private val userMetaDataRepository: UserMetaDataRepository,
    private val reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
    pagingFlow: Flow<PagingData<NoteWithUser>>,
) : MoleculeViewModel<FeedUiState>(recompositionClock) {

    private val feedPagingFlow = notePagingFlowMapper.map(pagingFlow)
        .cachedIn(viewModelScope)

    @Composable
    override fun models(): FeedUiState {
        return FeedUiState.Loaded(feedPagingFlow = feedPagingFlow)
    }

    open fun onNoteDisposed(id: String, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(pubkey.hex)
            reactionsRepository.stopSyncNoteReactions(id)
        }
    }

    open fun onNoteDisplayed(id: String, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(pubkey.hex)
            reactionsRepository.syncNoteReactions(id)
        }
    }

    fun onNoteReaction(noteId: NoteId) {
        viewModelScope.launch {
            reactionsRepository.sendReaction(noteId)
        }
    }
}
