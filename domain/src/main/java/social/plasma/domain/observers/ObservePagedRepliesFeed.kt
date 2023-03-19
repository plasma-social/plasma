package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import social.plasma.domain.PagingInteractor
import social.plasma.models.NoteWithUser
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedRepliesFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedRepliesFeed.Params, NoteWithUser>() {


    override fun createObservable(params: Params): Flow<PagingData<NoteWithUser>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = noteRepository::observePagedContactsReplies,
        ).flow.distinctUntilChanged()
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<NoteWithUser>
}
