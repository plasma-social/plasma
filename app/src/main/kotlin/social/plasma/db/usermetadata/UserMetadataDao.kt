package social.plasma.db.usermetadata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.models.UserMetaData

@Dao
interface UserMetadataDao {
    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubKey ORDER BY createdAt DESC")
    fun observeUserMetadata(pubKey: String): Flow<UserMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userMetadataEntity: UserMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userMetadata: Iterable<UserMetadataEntity>)

    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubKey ORDER BY createdAt DESC")
    fun newestMetadata(pubKey: String): UserMetadataEntity?

    @Transaction
    fun insertIfNewer(metadataList: Iterable<UserMetadataEntity>) {
        for (metadata in metadataList) {
            val currentNewest = newestMetadata(metadata.pubkey)
            if ((metadata.createdAt ?: 0) >= (currentNewest?.createdAt ?: 0)) {
                insert(metadata)
            }
        }
    }

    @Query("SELECT * FROM user_metadata WHERE pubkey = :pubkey")
    fun getById(pubkey: String): Flow<UserMetadataEntity?>
}
