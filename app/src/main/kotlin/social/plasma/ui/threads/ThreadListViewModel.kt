package social.plasma.ui.threads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import social.plasma.PubKey
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.ThreadRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.mappers.toNoteUiModel
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ThreadListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    threadRepository: ThreadRepository,
    private val userMetaDataRepository: UserMetaDataRepository,
    private val reactionsRepository: ReactionsRepository,
    @Named("default") defaultDispatcher: CoroutineContext,
) : ViewModel() {
    private val noteId: String = requireNotNull(savedStateHandle["noteId"])

    val uiState = threadRepository.observeThreadNotes(noteId)
        .debounce(100)
        .mapLatest { thread ->
            var initialFirstVisibleItem = 0

            val noteUiModels = thread.mapIndexed { index, noteWithUser ->
                if (noteWithUser.noteEntity.id == noteId) {
                    initialFirstVisibleItem = index
                    ThreadNoteUiModel.RootNote(noteWithUser.toNoteUiModel())
                } else {
                    ThreadNoteUiModel.LeafNote(noteWithUser.toNoteUiModel())
                }
            }

            ThreadUiState(
                noteUiModels = noteUiModels,
                firstVisibleItem = initialFirstVisibleItem,
            )
        }.flowOn(defaultDispatcher).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            ThreadUiState(emptyList())
        )

    fun onNoteDisplayed(noteId: String, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(pubkey.hex)
            reactionsRepository.syncNoteReactions(noteId)
        }
    }

    fun onNoteDisposed(noteId: String, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(pubkey.hex)
            reactionsRepository.syncNoteReactions(noteId)
        }
    }
}
