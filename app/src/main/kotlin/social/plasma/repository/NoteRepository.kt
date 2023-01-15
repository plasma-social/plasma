package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import javax.inject.Inject

interface NoteRepository {
    fun observeGlobalNotes(): Flow<NoteEntity>
}

class RealNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
) : NoteRepository {
    override fun observeGlobalNotes(): Flow<NoteEntity> {
        return noteDao.observeAllNotes()
    }
}
