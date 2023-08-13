package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import social.plasma.domain.PagingInteractor
import social.plasma.models.EventModel
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedRepliesFeed @Inject constructor(
    private val noteRepository: NoteRepository,
    private val scope: CoroutineScope,
) : PagingInteractor<ObservePagedRepliesFeed.Params, EventModel>() {


    override fun createObservable(params: Params): Flow<PagingData<EventModel>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = noteRepository::observePagedContactsReplies,
        ).flow.distinctUntilChanged().map {
            it.map { entity ->
                entity.toEventModel()
            }
        }.cachedIn(scope)
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<EventModel>
}
