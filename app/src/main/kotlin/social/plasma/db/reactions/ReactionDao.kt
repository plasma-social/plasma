package social.plasma.db.reactions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReactionDao {

    @Query("SELECT COUNT(id) FROM reactions WHERE noteId = :noteId")
    abstract fun observeNoteReactionCount(noteId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity: ReactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(list: Iterable<ReactionEntity>)
}
