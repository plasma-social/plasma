package social.plasma.domain.observers

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.decodeHex
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Event
import social.plasma.models.events.EventEntity
import social.plasma.nostr.relay.message.EventRefiner
import java.time.Instant
import javax.inject.Inject

class ObserveRelayCount @Inject constructor(
    private val contactsDao: ContactsDao,
    private val eventRefiner: EventRefiner,
) : SubjectInteractor<PubKey, Long>() {
    override fun createObservable(params: PubKey): Flow<Long> {
        return contactsDao.observeContactListEvent(params.hex()).filterNotNull()
            .map { eventRefiner.toRelayDetailList(it.toNostrEvent()) }
            .map { relayDetailList ->
                relayDetailList?.content?.size?.toLong() ?: 0
            }
    }
}

private fun EventEntity.toNostrEvent(): Event = Event(
    id = this.id.decodeHex(),
    pubKey = this.pubkey.decodeHex(),
    createdAt = Instant.ofEpochSecond(this.createdAt),
    kind = this.kind,
    content = this.content,
    tags = this.tags,
    sig = this.sig.decodeHex(),
)
