package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import social.plasma.models.UserMetadataEntity

@Dao
abstract class UserMetadataDao {
    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubKey")
    abstract fun observeUserMetadata(pubKey: String): Flow<UserMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertInternal(userMetadataEntity: UserMetadataEntity)

    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubKey ORDER BY createdAt DESC")
    internal abstract fun newestMetadata(pubKey: String): UserMetadataEntity?

    @Transaction
    open fun insert(metadata: UserMetadataEntity) {
        val currentNewest = newestMetadata(metadata.pubkey)
        if ((metadata.createdAt ?: 0) >= (currentNewest?.createdAt ?: 0)) {
            insertInternal(metadata)
        }
    }
}