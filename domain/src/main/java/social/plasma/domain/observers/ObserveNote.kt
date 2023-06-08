package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import social.plasma.domain.SubjectInteractor
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObserveNote @Inject constructor(
    private val noteRepository: NoteRepository,
) : SubjectInteractor<ObserveNote.Params, NoteWithUser?>() {
    data class Params(val id: NoteId)

    override fun createObservable(params: Params): Flow<NoteWithUser?> {
        return noteRepository.observeById(params.id)
    }
}
