package social.plasma.db.notes

import androidx.paging.PagingSource
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow

class FakeNoteDao : NoteDao {
    val noteWithUserTurbine = Turbine<NoteWithUser>()
    override fun insert(noteEntity: NoteEntity) {
        TODO("Not yet implemented")
    }

    override fun insert(noteEntity: Iterable<NoteEntity>) {
        TODO("Not yet implemented")
    }

    override fun userNotesAndRepliesPagingSource(pubkey: List<String>): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun globalNotesPagingSource(): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun getLatestNoteEpoch(pubkey: String, source: NoteSource): Flow<Long?> {
        TODO("Not yet implemented")
    }

    override fun getLatestNoteEpoch(source: NoteSource): Flow<Long?> {
        TODO("Not yet implemented")
    }

    override fun insertNoteReference(references: Iterable<NoteReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override fun observeThreadNotes(noteId: String): Flow<NoteThread?> {
        TODO("Not yet implemented")
    }

    override fun getParentNoteIds(noteId: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun notesBySource(source: NoteSource): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun notesAndRepliesBySource(source: NoteSource): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(noteId: String): NoteWithUser? {
        return noteWithUserTurbine.awaitItem()
    }

}
