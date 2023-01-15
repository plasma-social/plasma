package fakes

import androidx.paging.PagingSource
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import social.plasma.db.notes.NoteWithUserEntity

class FakeNoteDao : NoteDao {
    val inserts = Turbine<NoteEntity>()

    override fun insert(noteEntity: NoteEntity) {
        inserts.add(noteEntity)
    }

    override fun userNotesPagingSource(pubKey: String): PagingSource<Int, NoteWithUserEntity> {
        TODO("Not yet implemented")
    }

    override fun allNotesWithUsersPagingSource(): PagingSource<Int, NoteWithUserEntity> {
        TODO("Not yet implemented")
    }

    override fun observeAllNotes(): Flow<NoteEntity> {
        TODO("Not yet implemented")
    }
}
