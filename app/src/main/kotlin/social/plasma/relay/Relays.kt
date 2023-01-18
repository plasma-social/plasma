package social.plasma.relay

import android.util.Log
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.models.Contact
import social.plasma.models.Event
import social.plasma.models.Note
import social.plasma.models.Reaction
import social.plasma.models.TypedEvent
import social.plasma.models.UserMetaData
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.RelayMessage
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class Relays @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val scarletBuilder: Scarlet.Builder,
    @Named("default-relay-list") relayUrlList: List<String>,
    private val noteDao: NoteDao,
    private val userMetadataDao: UserMetadataDao,
    private val reactionDao: ReactionDao,
    private val eventRefiner: EventRefiner,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayList: List<Relay> = relayUrlList.map { createRelay(it, scope) }

    init {
        scope.launch {
            relayList.forEach { it.connectAndCollectMessage() }
        }
    }

    private suspend fun Relay.connectAndCollectMessage() {
        connect()
        flowRelayMessages().collect(::processMessage)
    }

    private fun processMessage(message: RelayMessage.EventRelayMessage) {
        when (message.event.kind) {
            Event.Kind.Note -> saveNote(eventRefiner.toNote(message)!!)
            Event.Kind.MetaData -> saveUserMetadata(eventRefiner.toUserMetaData(message)!!)
            Event.Kind.ContactList -> saveContact(eventRefiner.toContacts(message)!!)
            Event.Kind.Reaction -> saveReaction(eventRefiner.toReaction(message)!!)
        }
    }

    private fun saveReaction(reaction: TypedEvent<Reaction>) {
        val reactionEntity = reaction.toReactionEntity()
        if (reactionEntity != null) {
            reactionDao.insert(reactionEntity)
        }
    }

    private fun saveNote(relayMessage: TypedEvent<Note>) {
        noteDao.insert(relayMessage.toNoteEntity())

    }

    private fun saveUserMetadata(metadata: TypedEvent<UserMetaData>) {
        userMetadataDao.insert(metadata.toUserMetadataEntity())
    }

    private fun saveContact(contact: TypedEvent<Set<Contact>>) {
        Log.d("@@@", "Saving contact")
    }

    fun subscribe(request: SubscribeMessage): List<UnsubscribeMessage> {
        return relayList.map { it.subscribe(request) }
    }

    private fun createRelay(url: String, scope: CoroutineScope): Relay = Relay(
        url,
        scarletBuilder
            .webSocketFactory(okHttpClient.newWebSocketFactory(url))
            .build()
            .create(),
        scope
    )

    fun unsubscribe(unsubscribeMessage: UnsubscribeMessage) {
        relayList.forEach { it.unsubscribe(unsubscribeMessage) }
    }
}

private fun TypedEvent<Reaction>.toReactionEntity(): ReactionEntity? {
    val noteId = noteIdFromTags()
    noteId ?: return null

    return ReactionEntity(
        id = id.hex(),
        content = content.text,
        createdAt = createdAt.toEpochMilli(),
        noteId = noteId
    )
}

private fun TypedEvent<Reaction>.noteIdFromTags(): String? {
    // TODO we need to figure out what to do with the rest of the tags :/
    val noteId = this.tags.lastOrNull { it.firstOrNull() == "e" }?.getOrNull(1)
    return noteId
}

private fun TypedEvent<UserMetaData>.toUserMetadataEntity(): UserMetadataEntity =
    UserMetadataEntity(
        pubkey = pubKey.hex(),
        name = content.name,
        about = content.about,
        picture = content.picture,
        displayName = content.displayName,
        createdAt = createdAt.toEpochMilli(),
    )

private fun TypedEvent<Note>.toNoteEntity(): NoteEntity = NoteEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.toEpochMilli(),
    content = content.text,
    sig = sig.hex(),
)
