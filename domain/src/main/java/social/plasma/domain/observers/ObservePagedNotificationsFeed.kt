package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import social.plasma.domain.PagingInteractor
import social.plasma.models.EventModel
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedNotificationsFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedNotificationsFeed.Params, EventModel>() {

    override fun createObservable(params: Params): Flow<PagingData<EventModel>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = noteRepository::observePagedNotifications,
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toEventModel()
            }
        }
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<EventModel>
}
