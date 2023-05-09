package social.plasma.domain.observers

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import social.plasma.domain.PagingInteractor
import social.plasma.models.NoteWithUser
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservePagedFollowingFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedFollowingFeed.Params, NoteWithUser>() {

    init {
        Log.d("@@@", "ObservePagedFollowingFeed.init")
    }

    private val pagingDataFlow = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            prefetchDistance = 5,
            jumpThreshold = 10,
        ),
        pagingSourceFactory = noteRepository::observePagedContactsNotes,
    ).flow.distinctUntilChanged()

    override fun createObservable(params: Params): Flow<PagingData<NoteWithUser>> {
        Log.d("@@@", "ObservePagedFollowingFeed.createObservable")
        return pagingDataFlow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<NoteWithUser>
}
