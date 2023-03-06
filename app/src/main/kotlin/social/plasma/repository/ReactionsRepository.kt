package social.plasma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
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
}

@Singleton
class RealReactionsRepository @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineContext,
    private val relays: Relay,
    private val accountStateRepository: AccountStateRepository,
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
            val myPubkey = accountStateRepository.getPublicKey()!!
            val mySecretKey = accountStateRepository.getSecretKey()

            mySecretKey ?: return@withContext

            val event = Event.createEvent(
                pubKey = myPubkey.toByteString(),
                secretKey = mySecretKey.toByteString(),
                createdAt = Instant.now(),
                kind = Event.Kind.Reaction,
                tags = listOf(
                    listOf("e", noteId)
                ),
                content = reaction,
            )

            relays.send(ClientMessage.EventMessage(event = event))
        }
    }
}
