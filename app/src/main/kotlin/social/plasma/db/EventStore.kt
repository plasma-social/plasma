package social.plasma.db

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import social.plasma.db.events.EventReferenceEntity
import social.plasma.db.events.EventsDao
import social.plasma.db.events.PubkeyReferenceEntity
import social.plasma.db.ext.toEventEntity
import social.plasma.nostr.relay.Relays
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.utils.chunked
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.streams.toList

@Singleton
class EventStore @Inject constructor(
    private val eventsDao: EventsDao,
    private val relays: Relays,
    @Named("io") private val ioDispatcher: CoroutineContext,
) {
    suspend fun sync() = withContext(ioDispatcher) {
        relays.relayMessages.mapNotNull { (it as? RelayMessage.EventRelayMessage)?.event }
            .map { it.toEventEntity() }
            .chunked(1000, 200)
            .collect { events ->
                val noteReferences = events.parallelStream().flatMap { event ->
                    event.tags.parallelStream().filter { it.firstOrNull() == "e" }.map { tag ->
                        EventReferenceEntity(
                            sourceEvent = event.id,
                            targetEvent = tag[1],
                            relayUrl = tag.getOrNull(2),
                            marker = tag.getOrNull(3)?.toMarkerOrNull(),
                        )
                    }
                }

                val pubkeyReferences = events.parallelStream().flatMap { event ->
                    event.tags.parallelStream().filter { it.firstOrNull() == "p" }.map {
                        PubkeyReferenceEntity(
                            event.id,
                            pubkey = it[1],
                            relayUrl = it.getOrNull(2)
                        )
                    }
                }

                eventsDao.insertEventReferences(noteReferences.toList())
                eventsDao.insertPubkeyReferences(pubkeyReferences.toList())
                eventsDao.insert(events)
            }
    }

    private fun String?.toMarkerOrNull(): EventReferenceEntity.EventMarker? = when (this) {
        "reply" -> EventReferenceEntity.EventMarker.Reply
        "mention" -> EventReferenceEntity.EventMarker.Mention
        "root" -> EventReferenceEntity.EventMarker.Root
        else -> null
    }
}
