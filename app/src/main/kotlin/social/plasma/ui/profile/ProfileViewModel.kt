package social.plasma.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.nostr.models.UserMetaData
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.opengraph.OpenGraphParser
import social.plasma.prefs.Preference
import social.plasma.repository.ContactListRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.RealUserMetaDataRepository
import social.plasma.ui.mappers.NotePagingFlowMapper
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noteRepository: NoteRepository,
    private val userMetaDataRepository: RealUserMetaDataRepository,
    private val openGraphParser: OpenGraphParser,
    @UserKey(KeyType.Public) pubkeyPref: Preference<ByteArray>,
    contactListRepository: ContactListRepository,
    private val reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
) : ViewModel() {
    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))

    private val userNotesPagingFlow =
        notePagingFlowMapper.map(noteRepository.observeProfileNotes(profilePubKey.hex))
            .cachedIn(viewModelScope)

    private val myPubkey = PubKey.of(pubkeyPref.get(null)!!)

    private val followingState =
        contactListRepository
            .observeFollowState(myPubkey.hex, contactPubKey = profilePubKey.hex)
            .distinctUntilChanged()

    private val followingCount =
        contactListRepository.observeFollowingCount(profilePubKey.hex)
            .distinctUntilChanged()

    private val followersCount = contactListRepository.observeFollowersCount(profilePubKey.hex)
        .distinctUntilChanged()

    private val statCards = combine(followingCount, followersCount) { following, followers ->
        listOf(
            ProfileUiState.Loaded.ProfileStat(
                label = "Following",
                value = "$following",
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Followers",
                value = "$followers",
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Relays",
                value = "11",
            )
        )
    }

    private val initialState = ProfileUiState.Loaded(
        userNotesPagingFlow = userNotesPagingFlow,
        statCards = emptyList(),
        userData = ProfileUiState.Loaded.UserData(
            petName = profilePubKey.shortBech32,
            username = null,
            about = null,
            nip5Identifier = null,
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=${profilePubKey.hex}",
            publicKey = profilePubKey,
            website = null,
            banner = nostrichImage,
            lud = null,
        ),
    )


    private val userMetadata = merge(
        userMetaDataRepository.observeUserMetaData(profilePubKey.hex).filterNotNull(),
        contactListRepository.syncContactList(profilePubKey.hex),
    ).filterIsInstance<UserMetaData>()

    val uiState = combine(
        followingState,
        userMetadata,
        statCards
    ) { followState, metadata, profileStats ->

        ProfileUiState.Loaded(
            userNotesPagingFlow = userNotesPagingFlow,
            statCards = profileStats,
            userData = ProfileUiState.Loaded.UserData(
                petName = metadata.displayName ?: profilePubKey.shortBech32,
                banner = metadata.banner ?: nostrichImage,
                website = metadata.website,
                username = metadata.name?.let { "@$it" },
                about = metadata.about,
                nip5Identifier = metadata.nip05,
                nip5Domain = metadata.nip05?.split("@")?.getOrNull(1),
                avatarUrl = metadata.picture ?: "",
                publicKey = profilePubKey,
                lud = metadata.lud,
            ),
            following = followState,
            isNip5Valid = userMetaDataRepository::isNip5Valid
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)

    init {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(profilePubKey.hex, force = true)
        }
    }

    fun onNoteDisposed(id: NoteId) {
        viewModelScope.launch {
            reactionsRepository.stopSyncNoteReactions(id.hex)
        }
    }

    fun onNoteDisplayed(id: NoteId) {
        viewModelScope.launch {
            reactionsRepository.syncNoteReactions(id.hex)
        }
    }

    fun onNoteReaction(id: NoteId) {
        viewModelScope.launch {
            reactionsRepository.sendReaction(id.hex)
        }
    }

    suspend fun getOpenGraphMetadata(url: String): OpenGraphMetadata? {
        return runCatching { openGraphParser.parse(URL(url)) }.getOrNull()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(profilePubKey.hex)
        }
    }

    fun onRepostClick(noteId: NoteId) {
        viewModelScope.launch {
            reactionsRepository.repost(noteId.hex)
        }
    }

    companion object {
        private const val nostrichImage =
            "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096"
    }
}

