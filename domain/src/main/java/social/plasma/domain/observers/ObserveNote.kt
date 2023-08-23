package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import social.plasma.domain.SubjectInteractor
import social.plasma.models.EventModel
import social.plasma.models.NoteId
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class ObserveNote @Inject constructor(
    private val noteRepository: NoteRepository,
) : SubjectInteractor<ObserveNote.Params, EventModel?>() {
    data class Params(val id: NoteId)

    override fun createObservable(params: Params): Flow<EventModel?> {
        return noteRepository.observeEventById(params.id).map { it?.toEventModel() }
    }
}
