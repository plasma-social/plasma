package social.plasma.shared.repositories.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import social.plasma.models.Event
import social.plasma.models.HashTag
import social.plasma.models.events.EventEntity
import social.plasma.shared.repositories.api.ContactsRepository

class FakeContactsRepository : ContactsRepository {
    private val observeContactListEventResponse = MutableStateFlow(
        EventEntity(
            id = "1",
            pubkey = "pubkey",
            createdAt = System.currentTimeMillis() / 1000,
            kind = Event.Kind.ContactList,
            tags = listOf(),
            content = "",
            sig = ""
        )
    )


    override suspend fun followPubkey(pubKeyHex: String) {
        observeContactListEventResponse.update {
            it.copy(
                tags = it.tags + setOf(listOf("p", pubKeyHex))
            )
        }
    }

    override suspend fun unfollowPubkey(pubKeyHex: String) {
        observeContactListEventResponse.update {
            it.copy(
                tags = it.tags - setOf(listOf("p", pubKeyHex))
            )
        }
    }

    override suspend fun followHashTag(hashTag: HashTag) {
        observeContactListEventResponse.update {
            it.copy(
                tags = it.tags + setOf(listOf("t", hashTag.name))
            )
        }
    }

    override suspend fun unfollowHashTag(hashTag: HashTag) {
        observeContactListEventResponse.update {
            it.copy(
                tags = it.tags - setOf(listOf("t", hashTag.name))
            )
        }
    }

    override fun observeContactListEvent(pubKeyHex: String): Flow<EventEntity?> {
        return observeContactListEventResponse
    }
}
