package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import social.plasma.data.daos.LastRequestDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.HashTag
import social.plasma.models.Request
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObserveHashTagNewNoteCount @Inject constructor(
    private val noteRepository: NoteRepository,
    private val lastRequestDao: LastRequestDao,
) : SubjectInteractor<ObserveHashTagNewNoteCount.Params, Long>() {

    data class Params(
        val hashtag: HashTag,
    )

    override fun createObservable(params: Params): Flow<Long> {
        return lastRequestDao.observeLastRequest(Request.VIEW_HASHTAG, params.hashtag.name)
            .flatMapLatest {
                noteRepository.observeHashTagNoteCount(
                    params.hashtag,
                    since = it?.timestamp
                )
            }
    }
}
