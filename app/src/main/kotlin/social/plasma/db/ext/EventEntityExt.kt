package social.plasma.db.ext

import okio.ByteString.Companion.decodeHex
import social.plasma.db.events.EventEntity
import social.plasma.nostr.models.Event
import java.time.Instant

internal fun EventEntity.toNostrEvent() = Event(
    id = id.decodeHex(),
    content = content,
    createdAt = Instant.ofEpochSecond(createdAt),
    pubKey = pubkey.decodeHex(),
    sig = sig.decodeHex(),
    kind = kind,
    tags = tags,
)

internal fun Event.toEventEntity() = EventEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.epochSecond,
    kind = kind,
    tags = tags,
    content = content,
    sig = sig.hex(),
)
