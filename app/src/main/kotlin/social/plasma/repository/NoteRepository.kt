package social.plasma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import fr.acinq.secp256k1.Hex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.crypto.Bech32
import social.plasma.crypto.KeyPair
import social.plasma.db.notes.*
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.models.PubKey
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import social.plasma.prefs.Preference
import social.plasma.utils.chunked
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.streams.toList

interface NoteRepository {
    fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>>

    fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotes(): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>>

    suspend fun refreshContactsNotes(): List<NoteEntity>

    fun observeMentions(): Flow<PagingData<NoteWithUser>>

    suspend fun postNote(content: String)

    suspend fun replyToNote(noteId: String, content: String)
    suspend fun getById(noteId: String): NoteWithUser?
}


class RealNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val contactListRepository: ContactListRepository,
    @UserKey(KeyType.Public) private val myPubKey: Preference<ByteArray>,
    @UserKey(KeyType.Secret) private val mySecretKey: Preference<ByteArray>,
    private val eventRefiner: EventRefiner,
    private val relay: Relay,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : NoteRepository {
    private val bech32Regex = Regex("(@npub|@note|npub|note)[0-9a-z]{1,83}")

    private val myKeyPair by lazy {
        KeyPair(
            myPubKey.get(null)!!.toByteString(),
            mySecretKey.get(null)!!.toByteString()
        )
    }

    override fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.globalNotesPagingSource() }
            ).flow,

            fetchWithNoteDbSync(
                SubscribeMessage(Filter.globalFeedNotes),
                NoteSource.Global
            )

        ).filterIsInstance()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.userNotesAndRepliesPagingSource(pubkey) }
            ).flow,

            noteDao.getLatestNoteEpoch(pubkey).take(1)
                .map { epoch -> epoch?.let { Instant.ofEpochSecond(it) } ?: Instant.EPOCH }
                .flatMapLatest { since ->
                    fetchWithNoteDbSync(
                        subscribeMessage = SubscribeMessage(Filter.userNotes(pubkey, since)),
                        source = NoteSource.Profile
                    )
                }
                .flowOn(ioDispatcher),
        ).filterIsInstance()
    }


    override fun observeContactsNotes(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = {
                    noteDao.userContactNotesPagingSource(myPubkey)
                }
            ).flow,

            contactListRepository.observeContactLists(myPubkey).flatMapLatest {
                fetchWithNoteDbSync(
                    SubscribeMessage(Filter.userNotes(it.map { it.pubKey.hex() }.toSet())),
                    NoteSource.Contacts
                )
            }

        ).filterIsInstance()
    }

    override fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = {
                    noteDao.userContactNotesAndRepliesPagingSource(myPubkey)
                }
            ).flow,

            contactListRepository.observeContactLists(myPubkey)
                .filter { it.isNotEmpty() }.distinctUntilChanged().flatMapLatest {
                    val contactNpubList = it.map { it.pubKey.hex() }

                    fetchWithNoteDbSync(
                        SubscribeMessage(Filter.userNotes(contactNpubList.toSet())),
                        NoteSource.Contacts
                    )
                }
        ).filterIsInstance()

    }

    override suspend fun refreshContactsNotes(): List<NoteEntity> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        val latestRefresh = noteDao.getLatestNoteEpoch(NoteSource.Contacts)
            .firstOrNull()
        val since = latestRefresh?.let { Instant.ofEpochSecond(it) } ?: Instant.EPOCH
        val contacts = contactListRepository.syncContactList(pubkey = myPubkey).first()
        val contactNpubList = contacts.map { it.pubKey.hex() }
        return fetchWithNoteDbSync(
            SubscribeMessage(
                Filter.userNotes(
                    pubKeys = contactNpubList.toSet(),
                    since = since,
                )
            ),
            NoteSource.Contacts
        ).first()
    }

    override fun observeMentions(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = {
                    noteDao.pubkeyMentions(myPubkey)
                }
            ).flow,

            fetchWithNoteDbSync(
                SubscribeMessage(
                    Filter(
                        pTags = setOf(myPubkey),
                        kinds = setOf(Event.Kind.Note)
                    )
                ),
                NoteSource.Notifications
            )
        ).filterIsInstance()
    }

    override suspend fun postNote(content: String) = withContext(ioDispatcher) {
        val (contentWithPlaceholders, tags) = replaceContentWithPlaceholders(content)

        relay.sendNote(
            contentWithPlaceholders.trim(),
            myKeyPair,
            tags,
        )
    }

    override suspend fun replyToNote(noteId: String, content: String) = withContext(ioDispatcher) {
        val parentNote = noteDao.getById(noteId)?.noteEntity
        parentNote ?: return@withContext

        val replyTags = mutableSetOf<List<String>>().apply {
            parentNote.tags.forEachIndexed { index, tag ->
                if (tag.firstOrNull() == "p" || (tag.firstOrNull() == "e" && index == 0)) {
                    add(tag)
                }
            }

            add(listOf("e", parentNote.id))
            add(listOf("p", parentNote.pubkey))
        }

        val (contentWithPlaceholders, tags) = replaceContentWithPlaceholders(content, replyTags)

        relay.sendNote(contentWithPlaceholders.trim(), myKeyPair, tags)
    }


    private fun replaceContentWithPlaceholders(
        content: String,
        additionalTags: Set<List<String>> = emptySet(),
    ): Pair<String, Set<List<String>>> {
        // TODO Make it efficient 🤷🏽‍
        val additionalPTags = additionalTags.filter { it.firstOrNull() == "p" }.toSet()
        val additionalETags = additionalTags.filter { it.firstOrNull() == "e" }.toSet()

        val npubMentions = extractBech32Mentions(content, type = "npub")
        val noteMentions = extractBech32Mentions(content, type = "note")

        val allPTags = additionalPTags + npubMentions.map { listOf("p", it.second) }
        val allETags = additionalETags + noteMentions.map { listOf("e", it.second) }

        var contentWithPlaceholders =
            npubMentions.foldIndexed(content) { index, acc, (originalContent, hex) ->
                val placeholderIndex =
                    allETags.count() + allPTags.indexOfFirst { it.size >= 2 && it[1] == hex }
                acc.replace(originalContent, "#[$placeholderIndex]")
            }

        contentWithPlaceholders =
            noteMentions.foldIndexed(contentWithPlaceholders) { index, acc, (originalContent, hex) ->
                val placeholderIndex = allETags.indexOfFirst { it.size >= 2 && it[1] == hex }
                acc.replace(originalContent, "#[$placeholderIndex]")
            }

        val tags = allETags + allPTags

        return Pair(contentWithPlaceholders, tags)
    }


    private fun extractBech32Mentions(content: String, type: String): Set<Pair<String, String>> {
        return bech32Regex.findAll(content).map { matchResult ->
            val matchValue = with(matchResult.value) {
                if (this.startsWith("@"))
                    this.drop(1)
                else
                    this
            }

            val (hrp, bytes) = try {
                Bech32.decodeBytes(matchValue)
            } catch (e: Exception) {
                Timber.e("Invalid bech32")
                Triple(null, null, null)
            }

            val hex = when (hrp) {
                type -> Hex.encode(bytes!!)
                else -> null
            }

            hex?.let { Pair(matchResult.value, it) }
        }.filterNotNull().toSet()
    }

    override suspend fun getById(noteId: String): NoteWithUser? {
        return noteDao.getById(noteId)
    }

    private fun fetchWithNoteDbSync(subscribeMessage: SubscribeMessage, source: NoteSource) =
        relay.subscribe(subscribeMessage)
            .map { eventRefiner.toNote(it) }
            .filterNotNull()
            .map { it.toNoteEntity(source) }
            .chunked(500, 200)
            .onEach {
                val noteReferences = it.parallelStream().flatMap { note ->
                    val sourceNoteId = note.id

                    note.tags.parallelStream().filter { it.firstOrNull() == "e" }.map {
                        NoteReferenceEntity(sourceNoteId, targetNote = it[1])
                    }
                }

                val pubkeyReferences = it.parallelStream().flatMap { note ->
                    val sourceNoteId = note.id
                    note.tags.parallelStream().filter { it.firstOrNull() == "p" }.map {
                        PubkeyReferenceEntity(sourceNoteId, pubkey = it[1])
                    }
                }

                noteDao.insertNoteReferences(noteReferences.toList())
                noteDao.insertPubkeyReferences(pubkeyReferences.toList())
                noteDao.insert(it)
            }
            .flowOn(ioDispatcher)

    companion object {
        const val CHUNK_MAX_SIZE = 500
        const val CHUNK_DELAY = 1000L
    }
}

fun TypedEvent<Note>.toNoteEntity(
    source: NoteSource,
): NoteEntity = NoteEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.epochSecond,
    content = content.text,
    sig = sig.hex(),
    source = source,
    // TODO This isn't fool-proof.
    //  What happens with notes that mention other notes but aren't replies?
    isReply = tags.any { it.firstOrNull() == "e" },
    tags = tags,
)
