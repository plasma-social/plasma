package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import social.plasma.domain.PagingInteractor
import social.plasma.models.EventModel
import social.plasma.models.NoteId
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedThreadFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedThreadFeed.Params, EventModel>() {

    override fun createObservable(params: Params): Flow<PagingData<EventModel>> =
        Pager(
            config = params.pagingConfig,
            pagingSourceFactory = { noteRepository.observePagedThreadNotes(params.noteId) },
            initialKey = params.initialKey,
        ).flow.distinctUntilChanged().map { pagingData ->
            pagingData.map { it.toEventModel() }
        }


    data class Params(
        val noteId: NoteId,
        override val pagingConfig: PagingConfig,
        val initialKey: Int? = null,
    ) : Parameters<EventModel> {
    }
}
