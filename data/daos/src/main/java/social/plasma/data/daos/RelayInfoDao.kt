package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import social.plasma.models.RelayEntity

@Dao
interface RelayInfoDao {
    @Transaction
    fun replace(relayList: List<RelayEntity>) {
        deleteAll()
        upsert(relayList)
    }

    @Upsert
    fun upsert(relay: List<RelayEntity>)

    @Query("SELECT * FROM relays")
    fun observeRelays(): Flow<List<RelayEntity>>

    @Query("DELETE FROM relays")
    fun deleteAll()
}
