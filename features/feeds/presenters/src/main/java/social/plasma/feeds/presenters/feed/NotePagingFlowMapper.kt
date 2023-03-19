package social.plasma.feeds.presenters.feed

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.models.NoteWithUser
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class NotePagingFlowMapper @Inject constructor(
    private val noteCardMapper: NoteCardMapper,
    @Named("io") private val ioDispatcher: CoroutineContext,
) {
    fun map(pagingDataFlow: Flow<PagingData<NoteWithUser>>): Flow<PagingData<FeedItem>> {
        return pagingDataFlow.distinctUntilChanged().map { pagingData ->
            pagingData.map { note -> noteCardMapper.toFeedItem(note) }
        }.flowOn(ioDispatcher)
    }
}
