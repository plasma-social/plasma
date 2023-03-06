package social.plasma.db.notes

import androidx.paging.PagingSource
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow

class FakeNoteDao : NoteDao {
    val noteWithUserTurbine = Turbine<NoteWithUser>()

    override fun userNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun userContactNotesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun userContactNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun globalNotesPagingSource(): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }

    override fun observeThreadNotes(noteId: String): Flow<NoteThread?> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(noteId: String): NoteWithUser? {
        return noteWithUserTurbine.awaitItem()
    }

    override fun pubkeyMentions(pubkey: String): PagingSource<Int, NoteWithUser> {
        TODO("Not yet implemented")
    }
}
