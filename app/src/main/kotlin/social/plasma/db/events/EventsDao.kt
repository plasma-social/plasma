package social.plasma.db.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface EventsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(events: Iterable<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventReferences(references: Iterable<EventReferenceEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPubkeyReferences(references: Iterable<PubkeyReferenceEntity>)
}
