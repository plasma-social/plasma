package social.plasma.repository

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.db.events.EventsDao
import social.plasma.db.ext.toNostrEvent
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.prefs.Preference
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface ReactionsRepository {
    suspend fun syncNoteReactions(noteId: String)

    suspend fun stopSyncNoteReactions(noteId: String)

    suspend fun sendReaction(
        noteId: String,
        reaction: String = "🤙",
    )

    suspend fun repost(noteId: String)
}

@Singleton
class RealReactionsRepository @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineContext,
    private val relays: Relay,
    @UserKey(KeyType.Public) private val myPubkey: Preference<ByteArray>,
    @UserKey(KeyType.Secret) private val mySecretKey: Preference<ByteArray>,
    private val moshi: Moshi,
    private val eventsDao: EventsDao,
) : ReactionsRepository {
    private val noteIdsToObserve = AtomicReference<Set<String>>(emptySet())
    private val noteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        noteIdsFlow.debounce(500)
            .filter { it.isNotEmpty() }
            .flatMapLatest { noteIds ->
                relays.subscribe(
                    SubscribeMessage(
                        filter = Filter(
                            kinds = setOf(Event.Kind.Reaction),
                            eTags = noteIds,
                        )
                    )
                )
            }.launchIn(scope)
    }

    override suspend fun syncNoteReactions(noteId: String) {
        noteIdsFlow.update {
            noteIdsToObserve.updateAndGet {
                it + noteId
            }
        }
    }

    override suspend fun stopSyncNoteReactions(noteId: String) {
        noteIdsToObserve.updateAndGet { it - noteId }
    }

    override suspend fun sendReaction(noteId: String, reaction: String) {
        withContext(ioDispatcher) {

            val secretKey = mySecretKey.get(null)
            val pubKey = myPubkey.get(null)!!

            secretKey ?: return@withContext

            val note = eventsDao.getById(noteId)

            note ?: return@withContext

            val noteTags = note.tags.filter {
                it.size >= 2 && (it[0] == "e" || it[0] == "p")
            }

            val event = Event.createEvent(
                pubKey = pubKey.toByteString(),
                secretKey = secretKey.toByteString(),
                createdAt = Instant.now(),
                kind = Event.Kind.Reaction,
                tags = noteTags + listOf(
                    listOf("e", noteId),
                    listOf("p", note.pubkey)
                ),
                content = reaction,
            )

            relays.send(ClientMessage.EventMessage(event = event))
        }
    }

    override suspend fun repost(noteId: String) = withContext(ioDispatcher) {
        val secretKey = mySecretKey.get(null)
        val pubKey = myPubkey.get(null)!!

        secretKey ?: return@withContext

        val note = eventsDao.getById(noteId)

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
                listOf("e", noteId, "", "root"),
                listOf("p", note.pubkey)
            ),
            content = noteJson,
        )

        relays.send(ClientMessage.EventMessage(event = event))

    }

    private fun Event.toJson(): String = moshi.adapter(Event::class.java).toJson(this)
}
