package social.plasma.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import social.plasma.PubKey
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.nostr.models.UserMetaData
import social.plasma.prefs.Preference
import social.plasma.repository.ContactListRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.RealUserMetaDataRepository
import social.plasma.ui.ext.noteCardsPagingFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noteRepository: NoteRepository,
    userMetaDataRepository: RealUserMetaDataRepository,
    @UserKey(KeyType.Public) pubkeyPref: Preference<ByteArray>,
    contactListRepository: ContactListRepository,
) : ViewModel() {
    private val fakeProfile =
        ProfilePreviewProvider().values.filterIsInstance(ProfileUiState.Loaded::class.java).first()

    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))

    private val userNotesPagingFlow =
        noteCardsPagingFlow(noteRepository.observeProfileNotes(profilePubKey.hex))

    private val myPubkey = PubKey.of(pubkeyPref.get(null)!!)

    private val followingState =
        contactListRepository
            .observeFollowState(myPubkey.hex, contactPubKey = profilePubKey.hex)
            .distinctUntilChanged()

    private val followingCount =
        contactListRepository.observeFollowingCount(profilePubKey.hex)
            .distinctUntilChanged()

    private val statCards = followingCount
        .mapLatest { followingCount ->
            listOf(
                ProfileUiState.Loaded.ProfileStat(
                    label = "Following",
                    value = "$followingCount",
                ),
                ProfileUiState.Loaded.ProfileStat(
                    label = "Followers",
                    value = "2M",
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
        ),
    )


    private val userMetadata = merge(
        userMetaDataRepository.observeUserMetaData(profilePubKey.hex).filterNotNull(),
        userMetaDataRepository.syncUserMetadata(profilePubKey.hex),
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
                username = metadata.name?.let { "@$it" },
                about = metadata.about,
                nip5 = null,
                avatarUrl = metadata.picture ?: "",
                publicKey = profilePubKey,
            ),
            following = followState,
            statCards = profileStats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)


    fun onNoteDisposed(id: String) {
        // TODO cancel coroutine
    }

    fun onNoteDisplayed(id: String) {
        // TODO  move to repo
//        noteRepository.observeNoteReactionCount(id).launchIn(viewModelScope)
    }
}

