package social.plasma.db.contacts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contacts: Iterable<ContactEntity>)

    @Query("SELECT * FROM contacts WHERE owner = :pubkey")
    fun observeContacts(pubkey: String): Flow<List<ContactEntity>>

    @Query("DELETE FROM contacts WHERE owner = :owner")
    fun delete(owner: String)
}