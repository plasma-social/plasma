package social.plasma.domain.interactors

import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.EventsDao
import social.plasma.domain.Interactor
import social.plasma.models.Event
import social.plasma.models.NoteId
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SendNoteReaction @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineContext,
    private val accountStateRepository: AccountStateRepository,
    private val noteRepository: NoteRepository,
    private val relay: Relay,
) : Interactor<SendNoteReaction.Params>() {
    data class Params(
        val noteId: NoteId,
        val reaction: String = "🤙",
    )

    override suspend fun doWork(params: Params) {
        withContext(ioDispatcher) {

            val secretKey = accountStateRepository.getSecretKey()
            val pubKey = accountStateRepository.getPublicKey()!!

            secretKey ?: throw IllegalArgumentException("Secret key required")

            val note = noteRepository.getById(params.noteId)?.noteEntity

            note ?: throw IllegalArgumentException("Note not found")

            val noteTags = note.tags.filter {
                it.size >= 2 && (it[0] == "e" || it[0] == "p")
            }


            val event = Event.createEvent(
                pubKey = pubKey.toByteString(),
                secretKey = secretKey.toByteString(),
                createdAt = Instant.now(),
                kind = Event.Kind.Reaction,
                tags = noteTags + listOf(
                    listOf("e", params.noteId.hex),
                    listOf("p", note.pubkey)
                ),
                content = params.reaction,
            )

            relay.send(EventMessage(event = event))
        }
    }
}