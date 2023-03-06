package social.plasma.db.notes

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey = :pubkey")
    fun userNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey) AND NOT is_reply")
    fun userContactNotesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey)")
    fun userContactNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview")
    fun globalNotesPagingSource(): PagingSource<Int, NoteWithUser>
    
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    fun observeThreadNotes(noteId: String): Flow<NoteThread?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    suspend fun getById(noteId: String): NoteWithUser?

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM noteview n " +
                "LEFT JOIN pubkey_ref pr ON pr.source_event = n.id " +
                "WHERE pr.pubkey = :pubkey"
    )
    fun pubkeyMentions(pubkey: String): PagingSource<Int, NoteWithUser>
}

