package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import social.plasma.domain.PagingInteractor
import social.plasma.models.NoteWithUser
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedNotificationsFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedNotificationsFeed.Params, NoteWithUser>() {

    override fun createObservable(params: Params): Flow<PagingData<NoteWithUser>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = noteRepository::observePagedNotifications,
        ).flow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<NoteWithUser>
}
