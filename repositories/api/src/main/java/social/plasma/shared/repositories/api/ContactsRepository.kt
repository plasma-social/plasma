package social.plasma.shared.repositories.api

import kotlinx.coroutines.flow.Flow
import social.plasma.models.HashTag
import social.plasma.models.events.EventEntity

interface ContactsRepository {
    suspend fun followPubkey(pubKeyHex: String)

    suspend fun unfollowPubkey(pubKeyHex: String)

    suspend fun followHashTag(hashTag: HashTag)

    suspend fun unfollowHashTag(hashTag: HashTag)

    fun observeContactListEvent(pubKeyHex: String): Flow<EventEntity?>
}
