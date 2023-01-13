package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import social.plasma.models.Note
import social.plasma.models.TypedEvent
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner
import javax.inject.Inject

interface NoteRepository {
    fun observeGlobalNotes(): Flow<List<TypedEvent<Note>>>
}

class RealNoteRepository @Inject constructor(
    relays: Relays,
    private val eventRefiner: EventRefiner,
) : NoteRepository {

    private val notesSharedFlow: SharedFlow<List<TypedEvent<Note>>> = relays.sharedFlow {
        eventRefiner.toNote(it)
    }

    override fun observeGlobalNotes(): Flow<List<TypedEvent<Note>>> {
        return notesSharedFlow
    }
}
