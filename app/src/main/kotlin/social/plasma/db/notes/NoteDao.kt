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

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey = :pubKey")
    fun userNotesPagingSource(pubKey: String): PagingSource<Int, NoteWithUserEntity>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview")
    fun allNotesWithUsersPagingSource(): PagingSource<Int, NoteWithUserEntity>

    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    @RewriteQueriesToDropUnusedColumns
    fun observeAllNotes(): Flow<NoteEntity>
}
