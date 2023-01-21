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
    fun userNotesPagingSource(pubkey: List<String>): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview")
    fun allNotesWithUsersPagingSource(): PagingSource<Int, NoteWithUser>

    @Query("SELECT created_at FROM notes WHERE pubkey = :pubkey AND source = :source ORDER BY created_at DESC")
    fun getLatestNoteEpoch(pubkey: String, source: NoteSource = NoteSource.Profile): Flow<Long?>
}
