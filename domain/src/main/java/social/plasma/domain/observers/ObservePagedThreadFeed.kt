package social.plasma.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import social.plasma.domain.PagingInteractor
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import app.cash.nostrino.crypto.PubKey
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObservePagedThreadFeed @Inject constructor(
    private val noteRepository: NoteRepository,
) : PagingInteractor<ObservePagedThreadFeed.Params, NoteWithUser>() {

    override fun createObservable(params: Params): Flow<PagingData<NoteWithUser>> {
        return Pager(
            config = params.pagingConfig,
            pagingSourceFactory = { noteRepository.observePagedThreadNotes(params.noteId) },
        ).flow.distinctUntilChanged()
    }

    data class Params(
        val noteId: NoteId,
        override val pagingConfig: PagingConfig,
    ) : Parameters<NoteWithUser>
}
