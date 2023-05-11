package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request

@Dao
abstract class LastRequestDao {
    @Query("SELECT * FROM last_requests WHERE request = :request AND resource_id = :resourceId")
    abstract suspend fun lastRequest(request: Request, resourceId: String): LastRequestEntity?

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: LastRequestEntity): Long

    @Query("DELETE FROM last_requests WHERE request in (:requests)")
    abstract suspend fun purgeRequests(requests: List<Request>)
}
