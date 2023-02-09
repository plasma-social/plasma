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
    @Query("SELECT * FROM noteview WHERE pubkey IN (:pubkey)")
    fun userNotesAndRepliesPagingSource(pubkey: List<String>): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview")
    fun globalNotesPagingSource(): PagingSource<Int, NoteWithUser>


    @Query("SELECT created_at FROM notes WHERE pubkey = :pubkey AND source = :source ORDER BY created_at DESC")
    fun getLatestNoteEpoch(pubkey: String, source: NoteSource = NoteSource.Profile): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNoteReference(references: Iterable<NoteReferenceEntity>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    fun observeThreadNotes(noteId: String): Flow<NoteThread>

    @Query("SELECT targetNote FROM note_ref WHERE sourceNote = :noteId")
    fun getParentNoteIds(noteId: String): List<String>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE NOT(is_reply) AND source = :source")
    fun notesBySource(source: NoteSource): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE source = :source")
    fun notesAndRepliesBySource(source: NoteSource): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    suspend fun getById(noteId: String): NoteWithUser?
}

