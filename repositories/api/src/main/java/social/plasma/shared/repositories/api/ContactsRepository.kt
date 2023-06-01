package social.plasma.shared.repositories.api

import kotlinx.coroutines.flow.Flow
import social.plasma.models.events.EventEntity

interface ContactsRepository {
    suspend fun followPubkey(pubKeyHex: String)

    suspend fun unfollowPubkey(pubKeyHex: String)

    suspend fun followHashTag(hashTag: String)

    suspend fun unfollowHashTag(hashTag: String)
    
    fun observeContactListEvent(pubKeyHex: String): Flow<EventEntity?>
}
