package social.plasma.ui.threads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.ThreadRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.mappers.NoteCardMapper
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ThreadListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    threadRepository: ThreadRepository,
    private val userMetaDataRepository: UserMetaDataRepository,
    private val openGraphParser: OpenGraphParser,
    private val reactionsRepository: ReactionsRepository,
    @Named("default") defaultDispatcher: CoroutineContext,
    private val noteCardMapper: NoteCardMapper,
) : ViewModel() {
    private val noteId: String = requireNotNull(savedStateHandle["noteId"])

    val uiState = threadRepository.observeThreadNotes(noteId)
        .mapLatest { thread ->
            var firstVisibleItem = Int.MAX_VALUE

            val noteUiModels = thread.mapIndexed { index, noteWithUser ->
                if (noteWithUser.noteEntity.id == noteId) {
                    firstVisibleItem = index
                    ThreadNoteUiModel.RootNote(noteCardMapper.toNoteUiModel(noteWithUser))
                } else {
                    ThreadNoteUiModel.LeafNote(
                        noteCardMapper.toNoteUiModel(noteWithUser),
                        showConnector = index < firstVisibleItem,
                    )
                }
            }

            ThreadUiState(
                noteUiModels = noteUiModels,
                firstVisibleItem = firstVisibleItem,
            )
        }.flowOn(defaultDispatcher).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            ThreadUiState(emptyList())
        )

    fun onNoteDisplayed(noteId: NoteId, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(pubkey.hex)
            reactionsRepository.syncNoteReactions(noteId.hex)
        }
    }

    fun onNoteDisposed(noteId: NoteId, pubkey: PubKey) {
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(pubkey.hex)
            reactionsRepository.syncNoteReactions(noteId.hex)
        }
    }

    fun onNoteReaction(noteId: NoteId) {
        viewModelScope.launch {
            reactionsRepository.sendReaction(noteId.hex)
        }
    }

    suspend fun getOpenGraphMetadata(url: String): OpenGraphMetadata? {
        return runCatching { openGraphParser.parse(URL(url)) }.getOrNull()
    }
}
