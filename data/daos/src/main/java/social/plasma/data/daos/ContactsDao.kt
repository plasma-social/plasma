package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import social.plasma.models.ContactEntity
import social.plasma.models.Event
import social.plasma.models.events.EventEntity

@Dao
interface ContactsDao {
    @Transaction
    fun replace(contacts: Iterable<ContactEntity>) {
        delete()
        insert(contacts)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contacts: Iterable<ContactEntity>)


    @Query("SELECT * FROM contacts WHERE owner = :pubkey")
    fun observeContacts(pubkey: String): Flow<List<ContactEntity>>

    @Query("SELECT EXISTS(SELECT id FROM contacts WHERE owner = :ownerPubKey AND pubkey = :contactPubKey)")
    fun observeOwnerFollowsContact(ownerPubKey: String, contactPubKey: String): Flow<Boolean>

    @Query("SELECT * FROM events WHERE pubkey = :pubkey AND kind = ${Event.Kind.ContactList} ORDER BY created_at DESC")
    fun observeContactListEvent(pubkey: String): Flow<EventEntity>

    @Query("SELECT * FROM events WHERE pubkey = :pubkey AND kind = ${Event.Kind.ContactList} ORDER BY created_at DESC LIMIT 1")
    suspend fun getContactListEvent(pubkey: String): EventEntity?

    @Query("DELETE FROM contacts")
    fun delete()

    @Query("SELECT * FROM events WHERE kind = ${Event.Kind.ContactList} AND pubkey = :pubkey ORDER BY created_at DESC LIMIT 1")
    suspend fun getNewestEvent(pubkey: String): EventEntity?
}
