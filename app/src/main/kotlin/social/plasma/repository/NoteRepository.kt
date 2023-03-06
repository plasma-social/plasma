package social.plasma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import fr.acinq.secp256k1.Hex
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
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.prefs.Preference
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

interface NoteRepository {
    fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>>

    fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotes(): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>>

    suspend fun refreshContactsNotes(): List<RelayMessage.EventRelayMessage>

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
                config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, maxSize = DEFAULT_MAX_SIZE),
                pagingSourceFactory = { noteDao.globalNotesPagingSource() }
            ).flow,

            sync(
                SubscribeMessage(Filter.globalFeedNotes)
            )

        ).filterIsInstance()
    }


    override fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, maxSize = DEFAULT_MAX_SIZE),
                pagingSourceFactory = { noteDao.userNotesAndRepliesPagingSource(pubkey) }
            ).flow,

            sync(
                subscribeMessage = SubscribeMessage(
                    Filter.userNotes(
                        pubkey,
                        Instant.now().minus(14.days.toJavaDuration())
                    )
                )
            )

        ).filterIsInstance()
    }


    override fun observeContactsNotes(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, maxSize = DEFAULT_MAX_SIZE),
                pagingSourceFactory = {
                    noteDao.userContactNotesPagingSource(myPubkey)
                }
            ).flow,

            contactListRepository.observeContactLists(myPubkey).flatMapLatest {
                sync(SubscribeMessage(Filter.userNotes(it.map { it.pubKey.hex() }.toSet())))
            }

        ).filterIsInstance()
    }

    override fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE, maxSize = DEFAULT_MAX_SIZE),
                pagingSourceFactory = {
                    noteDao.userContactNotesAndRepliesPagingSource(myPubkey)
                }
            ).flow,

            contactListRepository.observeContactLists(myPubkey)
                .filter { it.isNotEmpty() }.distinctUntilChanged().flatMapLatest {
                    val contactNpubList = it.map { it.pubKey.hex() }

                    sync(SubscribeMessage(Filter.userNotes(contactNpubList.toSet())))
                }
        ).filterIsInstance()

    }

    override suspend fun refreshContactsNotes(): List<RelayMessage.EventRelayMessage> {
        // TODO Refresh contacts events
//        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex
//
//        val latestRefresh = noteDao.getLatestNoteEpoch(NoteSource.Contacts)
//            .firstOrNull()
//        val since = latestRefresh?.let { Instant.ofEpochSecond(it) } ?: Instant.EPOCH
//        val contacts = contactListRepository.syncContactList(pubkey = myPubkey).first()
//        val contactNpubList = contacts.map { it.pubKey.hex() }
//        return fetchWithNoteDbSync(
//            SubscribeMessage(
//                Filter.userNotes(
//                    pubKeys = contactNpubList.toSet(),
//                    since = since,
//                )
//            ),
//            NoteSource.Contacts
//        ).first()
        return emptyList()
    }

    override fun observeMentions(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(
                    pageSize = DEFAULT_PAGE_SIZE,
                    maxSize = DEFAULT_MAX_SIZE,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    noteDao.pubkeyMentions(myPubkey)
                }
            ).flow,

            sync(
                SubscribeMessage(
                    Filter(
                        pTags = setOf(myPubkey),
                        kinds = setOf(Event.Kind.Note, Event.Kind.Repost, Event.Kind.Reaction)
                    )
                )
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

    private fun sync(subscribeMessage: SubscribeMessage) =
        relay.subscribe(subscribeMessage)

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_MAX_SIZE = 150
    }
}

