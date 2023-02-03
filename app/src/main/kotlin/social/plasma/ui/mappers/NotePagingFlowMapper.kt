package social.plasma.ui.mappers

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import social.plasma.db.notes.NoteWithUser
import social.plasma.ui.components.notes.NoteUiModel
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class NotePagingFlowMapper @Inject constructor(
    private val noteCardMapper: NoteCardMapper,
    @Named("io") private val ioDispatcher: CoroutineContext,
) {
    fun map(pagingDataFlow: Flow<PagingData<NoteWithUser>>): Flow<PagingData<NoteUiModel>> {
        return pagingDataFlow.distinctUntilChanged()
            .map { pagingData ->
                pagingData.map { note -> noteCardMapper.toNoteUiModel(note) }
            }.flowOn(ioDispatcher)
    }
}
