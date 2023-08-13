package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import social.plasma.data.daos.LastRequestDao
import social.plasma.domain.PagingInteractor
import social.plasma.models.EventModel
import social.plasma.models.HashTag
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedHashTagFeed @Inject constructor(
    private val noteRepository: NoteRepository,
    private val lastRequestDao: LastRequestDao,
) : PagingInteractor<ObservePagedHashTagFeed.Params, EventModel>() {

    override fun createObservable(params: Params): Flow<PagingData<EventModel>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = { noteRepository.observePagedHashTagNotes(params.hashTag) },
        ).flow.distinctUntilChanged().onStart {
            lastRequestDao.upsert(
                LastRequestEntity(
                    request = Request.VIEW_HASHTAG,
                    resourceId = params.hashTag.name
                )
            )
        }.map { pagingData ->
            pagingData.map { entity ->
                entity.toEventModel()
            }
        }
    }

    data class Params(
        val hashTag: HashTag,
        override val pagingConfig: PagingConfig,
    ) : Parameters<EventModel>
}
