package social.plasma.db.contacts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contacts: Iterable<ContactEntity>)

    @Query("SELECT * FROM contacts WHERE owner = :pubkey")
    fun observeContacts(pubkey: String): Flow<List<ContactEntity>>

    @Query("SELECT EXISTS(SELECT id FROM contacts WHERE owner = :ownerPubKey AND pubkey = :contactPubKey)")
    fun observeOwnerFollowsContact(ownerPubKey: String, contactPubKey: String): Flow<Boolean>

    @Query("SELECT COUNT(id) FROM contacts WHERE owner = :pubkey")
    fun observeFollowingCount(pubkey: String): Flow<Long>

    @Query("DELETE FROM contacts WHERE owner = :owner")
    fun delete(owner: String)
}
