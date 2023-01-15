package social.plasma.db.usermetadata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMetadataDao {
    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubKey LIMIT 1")
    fun observeUserMetadata(pubKey: String): Flow<UserMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userMetadataEntity: UserMetadataEntity)
}
