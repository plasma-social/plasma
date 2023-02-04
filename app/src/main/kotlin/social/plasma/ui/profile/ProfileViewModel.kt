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
import social.plasma.PubKey
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.nostr.models.UserMetaData
import social.plasma.prefs.Preference
import social.plasma.repository.ContactListRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.RealUserMetaDataRepository
import social.plasma.ui.mappers.NotePagingFlowMapper
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noteRepository: NoteRepository,
    private val userMetaDataRepository: RealUserMetaDataRepository,
    @UserKey(KeyType.Public) pubkeyPref: Preference<ByteArray>,
    contactListRepository: ContactListRepository,
    private val reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
) : ViewModel() {
    private val fakeProfile =
        ProfilePreviewProvider().values.filterIsInstance(ProfileUiState.Loaded::class.java).first()

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
        statCards = fakeProfile.statCards,
        userData = ProfileUiState.Loaded.UserData(
            petName = profilePubKey.shortBech32,
            username = null,
            about = null,
            nip5 = null,
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=${profilePubKey.hex}",
            publicKey = profilePubKey,
            website = null,
            banner = nostrichImage
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
            userData = ProfileUiState.Loaded.UserData(
                petName = metadata.displayName ?: profilePubKey.shortBech32,
                banner = metadata.banner ?: nostrichImage,
                website = metadata.website,
                username = metadata.name?.let { "@$it" },
                about = metadata.about,
                nip5 = metadata.nip05,
                avatarUrl = metadata.picture ?: "",
                publicKey = profilePubKey,
            ),
            following = followState,
            statCards = profileStats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)

    init {
        viewModelScope.launch {
            userMetaDataRepository.syncUserMetadata(profilePubKey.hex, force = true)
        }
    }

    fun onNoteDisposed(id: String) {
        viewModelScope.launch {
            reactionsRepository.stopSyncNoteReactions(id)
        }
    }

    fun onNoteDisplayed(id: String) {
        viewModelScope.launch {
            reactionsRepository.syncNoteReactions(id)
        }
    }

    fun onNoteReaction(id: String) {
        viewModelScope.launch {
            reactionsRepository.sendReaction(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            userMetaDataRepository.stopUserMetadataSync(profilePubKey.hex)
        }
    }

    companion object {
        private const val nostrichImage =
            "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096"
    }
}

