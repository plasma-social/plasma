package social.plasma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.Reaction
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import social.plasma.utils.chunked
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
    eventRefiner: EventRefiner,
    private val relays: Relay,
    private val reactionDao: ReactionDao,
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
                ).map { event -> eventRefiner.toReaction(event) }
                    .map { event -> event?.toReactionEntity() }
                    .filterNotNull()
                    .chunked(RealNoteRepository.CHUNK_MAX_SIZE, RealNoteRepository.CHUNK_DELAY)
                    .onEach { reactionDao.insert(it) }
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

            reactionDao.insert(
                ReactionEntity(
                    id = event.id.hex(),
                    content = event.content,
                    createdAt = event.createdAt.epochSecond,
                    noteId = noteId,
                    pubkey = event.pubKey.hex(),
                )
            )

            relays.send(ClientMessage.EventMessage(event = event))
        }
    }
}

private fun TypedEvent<Reaction>.toReactionEntity(): ReactionEntity? {
    val noteId = noteIdFromTags()
    noteId ?: return null

    return ReactionEntity(
        id = id.hex(),
        content = content.text,
        createdAt = createdAt.epochSecond,
        noteId = noteId,
        pubkey = pubKey.hex(),
    )
}

private fun TypedEvent<Reaction>.noteIdFromTags(): String? {
    // TODO we need to figure out what to do with the rest of the tags :/
    val noteId = this.tags.lastOrNull { it.firstOrNull() == "e" }?.getOrNull(1)
    return noteId
}
