package social.plasma.domain.interactors

import com.squareup.moshi.Moshi
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.EventsDao
import social.plasma.domain.Interactor
import social.plasma.models.Event
import social.plasma.models.NoteId
import social.plasma.models.events.EventEntity
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class RepostNote @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineContext,
    private val moshi: Moshi,
    private val accountStateRepository: AccountStateRepository,
    private val relayManager: RelayManager,
    private val eventsDao: EventsDao,
) : Interactor<RepostNote.Params>() {
    data class Params(
        val noteId: NoteId,
    )

    override suspend fun doWork(params: Params) {
        withContext(ioDispatcher) {
            val secretKey = accountStateRepository.getSecretKey()
            val pubKey = accountStateRepository.getPublicKey()!!

            secretKey ?: return@withContext

            val note = eventsDao.getById(params.noteId.hex)

            note ?: return@withContext

            val noteJson = note.toNostrEvent().toJson()

            val noteTags = note.tags.filter {
                it.size >= 2 && (it[0] == "e" || it[0] == "p")
            }

            val event = Event.createEvent(
                pubKey = pubKey.toByteString(),
                secretKey = secretKey.toByteString(),
                createdAt = Instant.now(),
                kind = Event.Kind.Repost,
                tags = noteTags + listOf(
                    listOf("e", params.noteId.hex, "", "root"),
                    listOf("p", note.pubkey)
                ),
                content = noteJson,
            )

            relayManager.send(ClientMessage.EventMessage(event = event))
        }
    }

    private fun Event.toJson(): String = moshi.adapter(Event::class.java).toJson(this)
}


private fun EventEntity.toNostrEvent() = Event(
    id = id.decodeHex(),
    content = content,
    createdAt = Instant.ofEpochSecond(createdAt),
    pubKey = pubkey.decodeHex(),
    sig = sig.decodeHex(),
    kind = kind,
    tags = tags,
)
