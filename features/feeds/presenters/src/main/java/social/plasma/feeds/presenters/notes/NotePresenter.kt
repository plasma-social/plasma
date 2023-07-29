package social.plasma.feeds.presenters.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import shortBech32
import social.plasma.common.screens.AndroidScreens
import social.plasma.domain.interactors.GetLightningInvoice
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.Nip5Status
import social.plasma.domain.interactors.RepostNote
import social.plasma.domain.interactors.SendNoteReaction
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.domain.observers.ObserveUserMetadata
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
import social.plasma.models.HashTag
import social.plasma.models.Mention
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.repositories.api.UserMetadataRepository
import social.plasma.shared.utils.api.InstantFormatter
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.time.Instant

class NotePresenter @AssistedInject constructor(
    private val contentParser: NoteContentParser,
    private val getNip5Status: GetNip5Status,
    private val repostNote: RepostNote,
    private val observeUserMetadata: ObserveUserMetadata,
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
        val userPubkey = rememberRetained { PubKey(args.eventEntity.pubkey.decodeHex()) }
        LaunchedEffect(userPubkey) {
            syncMetadata.executeSync(SyncMetadata.Params(userPubkey))
        }

        val isNoteLiked by produceRetainedState<Boolean>(initialValue = false) {
            value = noteRepository.isNoteLiked(userPubkey, NoteId(args.eventEntity.id))
        }

        val noteContent: List<ContentBlock> by produceState(emptyList()) {
            value = contentParser.parseNote(
                args.eventEntity.content,
                args.eventEntity.tags.toIndexedMap()
            )
        }

        val cardLabel by produceRetainedState<String?>(initialValue = null) {
            value = buildBannerLabel(args.eventEntity.tags)
        }

        val userMetadata by remember {
            observeUserMetadata.flow.onStart {
                observeUserMetadata(
                    ObserveUserMetadata.Params(userPubkey)
                )
            }
        }.collectAsState(initial = null)

        val isNip5Valid by produceRetainedState(initialValue = false, userPubkey, userMetadata) {
            value = when (getNip5Status.executeSync(
                GetNip5Status.Params(
                    userPubkey,
                    userMetadata?.nip05
                )
            )) {
                Nip5Status.Invalid, Nip5Status.Missing -> false
                Nip5Status.Valid -> true
            }
        }

        val coroutineScope = rememberCoroutineScope()

        return NoteUiState(
            FeedItem.NoteCard(
                id = args.eventEntity.id,
                key = args.eventEntity.id,
                cardLabel = cardLabel,
                userPubkey = userPubkey,
                content = noteContent,
                name = userMetadata?.name ?: "",
                displayName = userMetadata?.userFacingName ?: "",
                isLiked = isNoteLiked,
                avatarUrl = userMetadata?.picture,
                nip5Identifier = userMetadata?.nip05,
                nip5Domain = userMetadata?.nip05?.split("@")?.getOrNull(1),
                timePosted = instantFormatter.getRelativeTime(Instant.ofEpochSecond(args.eventEntity.createdAt)),
                zapsEnabled = userMetadata?.tipAddress != null,
                isNip5Valid = { _, _ -> isNip5Valid }, // TODO change to variable
            )
        )
        { event ->
            when (event) {
                NoteUiEvent.OnAvatarClick -> navigator.goTo(ProfileScreen(userPubkey.hex()))
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
                                NoteId(args.eventEntity.id),
                            )
                        )
                    }
                }

                is NoteUiEvent.OnNoteClick -> navigator.goTo(ThreadScreen(event.noteId))
                is NoteUiEvent.OnProfileClick -> navigator.goTo(ProfileScreen(event.pubKey.hex()))
                NoteUiEvent.OnReplyClick -> navigator.goTo(
                    ComposingScreen(
                        noteType = ComposingScreen.NoteType.Reply(
                            NoteId(args.eventEntity.id)
                        )
                    )
                )

                NoteUiEvent.OnRepostClick -> coroutineScope.launch {
                    repostNote.executeSync(
                        RepostNote.Params(
                            NoteId(args.eventEntity.id),
                        )
                    )
                }

                is NoteUiEvent.OnZapClick -> {
                    coroutineScope.launch {
                        val tipAddress = userMetadata?.tipAddress
                        tipAddress ?: return@launch

                        if (event.satAmount <= 0) return@launch

                        getLightningInvoice.executeSync(
                            GetLightningInvoice.Params(
                                tipAddress,
                                amount = BitcoinAmount(sats = event.satAmount),
                                event = NoteId(args.eventEntity.id),
                                recipient = userPubkey,
                            )
                        ).onSuccess { data ->
                            navigator.goTo(AndroidScreens.ShareLightningInvoiceScreen(data.invoice))
                        }.onFailure {
                            //TODO show error
                            Timber.w(it)
                        }
                    }
                }

                NoteUiEvent.OnClick -> navigator.goTo(ThreadScreen(NoteId(args.eventEntity.id)))
                is NoteUiEvent.OnNestedNavEvent -> navigator.onNavEvent(event.navEvent)
            }
        }
    }

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
                    userMetaDataRepository.observeUserMetaData(pubkey).firstOrNull()?.name
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
                        (userMetaDataRepository.observeUserMetaData(pubkey).firstOrNull()?.name
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


    @AssistedFactory
    interface Factory {
        fun create(args: NoteScreen, navigator: Navigator): NotePresenter
    }

}
