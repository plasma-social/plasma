package social.plasma.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import social.plasma.db.notes.NoteDao
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.models.PubKey
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.ui.ext.noteCardsPagingFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noteDao: NoteDao,
    userMetadataDao: UserMetadataDao,
    private val relays: Relays,
) : ViewModel() {
    private val fakeProfile =
        ProfilePreviewProvider().values.filterIsInstance(ProfileUiState.Loaded::class.java).first()

    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))

    private val userNotesPagingFlow =
        noteCardsPagingFlow { noteDao.userNotesPagingSource(profilePubKey.value) }


    private val profileSubscriptions =
        relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(profilePubKey.value))) +
                relays.subscribe(SubscribeMessage(filters = Filters.userNotes(profilePubKey.value)))

    private val initialState = ProfileUiState.Loaded(
        userNotesPagingFlow = userNotesPagingFlow,
        userData = ProfileUiState.Loaded.UserData(
            petName = profilePubKey.shortBech32,
            username = null,
            about = null,
            nip5 = null,
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=${profilePubKey.value}",
            publicKey = profilePubKey,
        ),
        statCards = fakeProfile.statCards,
    )

    val uiState =
        userMetadataDao.observeUserMetadata(profilePubKey.value)
            .filterNotNull()
            .map {
                val publicKey = PubKey(it.pubkey)
                ProfileUiState.Loaded(
                    userNotesPagingFlow = userNotesPagingFlow,
                    userData = ProfileUiState.Loaded.UserData(
                        petName = it.displayName ?: publicKey.shortBech32,
                        username = it.name?.let { "@$it" },
                        about = it.about,
                        nip5 = null,
                        avatarUrl = it.picture ?: "",
                        publicKey = publicKey,
                    ),
                    statCards = fakeProfile.statCards,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)


    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.Default) {
            profileSubscriptions.forEach {
                relays.unsubscribe(it)
            }
        }
    }

    fun onNoteDisposed(id: String) {
        // TODO dispose subscriptions
    }

    fun onNoteDisplayed(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            id to relays.subscribe(SubscribeMessage(filters = Filters.noteReactions(id)))
        }
    }
}

