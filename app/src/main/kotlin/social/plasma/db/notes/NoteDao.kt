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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(noteEntity: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(noteEntity: Iterable<NoteEntity>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey = :pubkey")
    fun userNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey) AND NOT is_reply")
    fun userContactNotesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey)")
    fun userContactNotesAndRepliesPagingSource(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview")
    fun globalNotesPagingSource(): PagingSource<Int, NoteWithUser>

    @Query("SELECT created_at FROM notes WHERE pubkey = :pubkey AND source = :source ORDER BY created_at DESC")
    fun getLatestNoteEpoch(pubkey: String, source: NoteSource = NoteSource.Profile): Flow<Long?>

    @Query("SELECT created_at FROM notes WHERE source = :source ORDER BY created_at DESC")
    fun getLatestNoteEpoch(source: NoteSource = NoteSource.Profile): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNoteReferences(references: Iterable<NoteReferenceEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPubkeyReferences(references: Iterable<PubkeyReferenceEntity>)

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
                "LEFT JOIN pubkey_ref pr ON pr.sourceNote = n.id " +
                "WHERE pr.pubkey = :pubkey"
    )
    fun pubkeyMentions(pubkey: String): PagingSource<Int, NoteWithUser>
}

