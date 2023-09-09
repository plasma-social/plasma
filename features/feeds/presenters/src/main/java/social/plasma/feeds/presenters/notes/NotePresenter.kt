package social.plasma.feeds.presenters.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import shortBech32
import social.plasma.common.screens.AndroidScreens
import social.plasma.domain.interactors.GetLightningInvoice
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.RepostNote
import social.plasma.domain.interactors.SendNoteReaction
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.domain.observers.toEventModel
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiEvent
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiState
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.feeds.presenters.feed.NoteContentParser
import social.plasma.models.BitcoinAmount
import social.plasma.models.Event
import social.plasma.models.EventModel
import social.plasma.models.HashTag
import social.plasma.models.Mention
import social.plasma.models.Nip5Status
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.repositories.api.UserMetadataRepository
import social.plasma.shared.utils.api.InstantFormatter
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class NotePresenter @AssistedInject constructor(
    private val contentParser: NoteContentParser,
    private val getNip5Status: GetNip5Status,
    private val repostNote: RepostNote,
    private val stringManager: StringManager,
    private val userMetaDataRepository: UserMetadataRepository,
    private val noteRepository: NoteRepository,
    private val getLightningInvoice: GetLightningInvoice,
    private val sendNoteReaction: SendNoteReaction,
    private val instantFormatter: InstantFormatter,
    private val syncMetadata: SyncMetadata,
    @Assisted private val args: NoteScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<NoteUiState> {

    @Composable
    override fun present(): NoteUiState {
        val eventPubkeyMetadata = observeEventPubkeyMetadata()
        val note = observeNoteEvent()

        val noteNotFound by produceState(initialValue = false, note) {
            delay(fetchNoteTimeOut)
            value = note == null
        }

        val notePubkey = observeNotePubkey(note)

        // In most cases, this will be the same was eventPubkeyMetadata.
        // However, it could be different if this note is a repost.
        val notePubkeyMetadata = observeNotePubkeyMetadata(notePubkey, eventPubkeyMetadata)

        val noteContent by produceState<List<ContentBlock>?>(
            null, note
        ) {
            value = note?.let {
                contentParser.parseNote(
                    note = note.content,
                    mentions = note.tags.toIndexedMap(),
                    tags = note.tags,
                    kind = note.kind,
                )
            }
        }

        return if (noteNotFound) {
            NoteUiState.NotFound
        } else if (notePubkey == null || note == null || noteContent == null) {
            NoteUiState.Loading
        } else {
            buildNoteUi(note, notePubkey, notePubkeyMetadata, eventPubkeyMetadata, noteContent!!)
        }
    }

    @Composable
    private fun buildNoteUi(
        note: EventModel,
        notePubkey: PubKey,
        notePubkeyMetadata: UserMetadataEntity?,
        eventPubkeyMetadata: UserMetadataEntity?,
        noteContent: List<ContentBlock>,
    ): NoteUiState.Loaded {
        val isNoteLiked by remember(note.id) { noteRepository.isNoteLiked(NoteId(note.id)) }.collectAsState(
            initial = false
        )

        val likeCount by remember {
            noteRepository.observeLikeCount(NoteId(note.id))
        }.collectAsState(initial = 0)


        val cardLabel by produceState<String?>(initialValue = null, note) {
            value = buildBannerLabel(note.tags)
        }

        val nip5Status by produceState<Nip5Status>(
            initialValue = Nip5Status.Missing,
            notePubkey,
            notePubkeyMetadata
        ) {
            val nip5Indentifier = notePubkeyMetadata?.nip05?.takeIf { it.isNotBlank() }

            if (nip5Indentifier == null) {
                value = Nip5Status.Missing
            } else {
                value = Nip5Status.Set.Loading(nip5Indentifier)
                value =
                    getNip5Status.executeSync(
                        GetNip5Status.Params(
                            notePubkey,
                            nip5Indentifier
                        )
                    )
            }
        }

        val headerContent by produceState<ContentBlock.Text?>(
            null,
            note,
            eventPubkeyMetadata
        ) {
            value = if (args.eventEntity.kind == Event.Kind.Repost) {
                val pubkey = PubKey(args.eventEntity.pubkey.decodeHex())
                val userFacingName = eventPubkeyMetadata?.userFacingName
                ContentBlock.Text(
                    "Boosted by #[0]",
                    mapOf(
                        0 to ProfileMention(
                            text = userFacingName ?: pubkey.shortBech32(),
                            pubkey = pubkey
                        )
                    )
                )
            } else null
        }

        val coroutineScope = rememberCoroutineScope()

        return NoteUiState.Loaded(
            FeedItem.NoteCard(
                id = note.id,
                key = note.id,
                cardLabel = cardLabel,
                userPubkey = notePubkey,
                content = noteContent,
                name = notePubkeyMetadata?.name ?: "",
                displayName = notePubkeyMetadata?.userFacingName ?: "",
                headerContent = headerContent,
                isLiked = isNoteLiked,
                likeCount = likeCount.toInt(),
                avatarUrl = notePubkeyMetadata?.picture,
                nip5Identifier = notePubkeyMetadata?.nip05,
                timePosted = instantFormatter.getRelativeTime(Instant.ofEpochSecond(note.createdAt)),
                zapsEnabled = notePubkeyMetadata?.tipAddress != null,
                nip5Status = nip5Status,
            ),
            style = args.style,
        )
        { event ->
            when (event) {
                NoteUiEvent.OnAvatarClick -> navigator.goTo(ProfileScreen(notePubkey.hex()))
                is NoteUiEvent.OnHashTagClick -> navigator.goTo(
                    HashTagFeedScreen(
                        HashTag.parse(
                            event.hashTag
                        )
                    )
                )

                NoteUiEvent.OnLikeClick -> {
                    coroutineScope.launch {
                        sendNoteReaction.executeSync(
                            SendNoteReaction.Params(
                                NoteId(note.id),
                            )
                        )
                    }
                }

                is NoteUiEvent.OnNoteClick -> navigator.goTo(ThreadScreen(event.noteId))
                is NoteUiEvent.OnProfileClick -> navigator.goTo(ProfileScreen(event.pubKey.hex()))
                NoteUiEvent.OnReplyClick -> navigator.goTo(
                    ComposingScreen(
                        noteType = ComposingScreen.NoteType.Reply(
                            NoteId(note.id)
                        )
                    )
                )

                NoteUiEvent.OnRepostClick -> coroutineScope.launch {
                    repostNote.executeSync(
                        RepostNote.Params(
                            NoteId(note.id),
                        )
                    )
                }

                is NoteUiEvent.OnZapClick -> {
                    coroutineScope.launch {
                        val tipAddress = notePubkeyMetadata?.tipAddress
                        tipAddress ?: return@launch

                        if (event.satAmount <= 0) return@launch

                        getLightningInvoice.executeSync(
                            GetLightningInvoice.Params(
                                tipAddress,
                                amount = BitcoinAmount(sats = event.satAmount),
                                event = NoteId(note.id),
                                recipient = notePubkey,
                            )
                        ).onSuccess { data ->
                            navigator.goTo(AndroidScreens.ShareLightningInvoiceScreen(data.invoice))
                        }.onFailure {
                            //TODO show error
                            Timber.w(it)
                        }
                    }
                }

                NoteUiEvent.OnClick -> navigator.goTo(ThreadScreen(NoteId(note.id)))
                is NoteUiEvent.OnNestedNavEvent -> navigator.onNavEvent(event.navEvent)
            }
        }
    }

    @Composable
    private fun observeNotePubkey(note: EventModel?) = produceState<PubKey?>(null, note) {
        val pubkey = note?.pubkey?.decodeHex()?.let { PubKey(it) }
        pubkey?.let {
            launch {
                syncMetadata.executeSync(SyncMetadata.Params(it))
            }
        }
        value = pubkey
    }.value

    @Composable
    private fun observeNoteEvent() = remember {
        if (args.eventEntity.kind == Event.Kind.Repost) {
            val repostedEventId =
                args.eventEntity.tags.firstOrNull { it.firstOrNull() == "e" }?.getOrNull(1)

            if (repostedEventId != null) {
                noteRepository.observeEventById(NoteId(repostedEventId))
                    .timeout(fetchNoteTimeOut)
                    .map { it?.toEventModel() }
                    .filterNotNull()
            } else {
                flowOf(args.eventEntity)
            }
        } else {
            flowOf(args.eventEntity)
        }
    }.collectAsState(null).value

    @Composable
    private fun observeNotePubkeyMetadata(
        notePubkey: PubKey?,
        eventPubkeyMetadata: UserMetadataEntity?,
    ) = remember(eventPubkeyMetadata, notePubkey) {
        if (args.eventEntity.kind == Event.Kind.Repost) {
            if (notePubkey != null) {
                userMetaDataRepository.observeUserMetaData(notePubkey)
            } else {
                flowOf(null)
            }
        } else {
            flowOf(eventPubkeyMetadata)
        }
    }.collectAsState(initial = null).value

    @Composable
    private fun observeEventPubkeyMetadata() = remember {
        userMetaDataRepository.observeUserMetaData(PubKey(args.eventEntity.pubkey.decodeHex()))
    }.collectAsState(initial = null).value

    private suspend fun buildBannerLabel(tags: List<List<String>>): String? {
        val pTags = tags.filter { it.firstOrNull() == "p" && it.size >= 2 }

        val referencedNames = getReferencedNames(pTags)

        return when {
            referencedNames.isEmpty() -> null
            referencedNames.size == 1 -> stringManager.getFormattedString(
                R.string.replying_to_single,
                mapOf("user" to referencedNames[0])
            )

            else -> {
                val additionalReferencedNames = pTags.size - 2
                stringManager.getFormattedString(
                    R.string.replying_to_many, mapOf(
                        "additionalUserCount" to additionalReferencedNames,
                        "firstUser" to referencedNames[0],
                        "secondUser" to referencedNames[1],
                    )
                )
            }
        }
    }

    private suspend fun getReferencedNames(tags: List<List<String>>): List<String> =
        coroutineScope {
            val referencedNames = mutableSetOf<Deferred<String>>()

            for (tag in tags) {
                val pubkey = PubKey(tag[1].decodeHex())
                val userNameDeferred = async {
                    userMetaDataRepository.observeUserMetaData(pubkey).firstOrNull()?.userFacingName
                        ?: pubkey.shortBech32()
                }

                referencedNames.add(userNameDeferred)

                if (referencedNames.size >= 2) break
            }

            referencedNames.awaitAll()
        }

    private suspend fun List<List<String>>.toIndexedMap(): Map<Int, Mention> {
        return mapIndexed { index, tag ->
            when (tag.firstOrNull()) {
                "p" -> {
                    val pubkey = PubKey(tag[1].decodeHex())

                    val userName =
                        (userMetaDataRepository.observeUserMetaData(pubkey)
                            .firstOrNull()?.userFacingName
                            ?: pubkey.shortBech32())


                    return@mapIndexed index to ProfileMention(
                        pubkey = pubkey,
                        text = "@$userName"
                    )
                }

                "e" -> {
                    val noteId = NoteId(tag[1])
                    val mentionText = try {
                        "@${noteId.shortBech32}"
                    } catch (e: Exception) {
                        Timber.e(e)
                        null
                    }

                    mentionText ?: return@mapIndexed null
                    return@mapIndexed index to NoteMention(
                        noteId = noteId,
                        text = mentionText
                    )
                }

                else -> null
            }
        }.filterNotNull().toMap()
    }

    companion object {
        private val fetchNoteTimeOut = 5.seconds
    }


    @AssistedFactory
    interface Factory {
        fun create(args: NoteScreen, navigator: Navigator): NotePresenter
    }

}
