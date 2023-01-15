package social.plasma.ui.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))

    private val userNotesPagingFlow =
        noteCardsPagingFlow { noteDao.userNotesPagingSource(profilePubKey.value) }


    private val unsubscribeMessages =
        relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(profilePubKey.value))) +
                relays.subscribe(SubscribeMessage(filters = Filters.userNotes(profilePubKey.value)))


    private val initialState = ProfileUiState.Loaded(
        userNotesPagingFlow = userNotesPagingFlow,
        userData = ProfileUiState.Loaded.UserData(
            petName = "${profilePubKey.value.take(8)}...",
            username = null,
            bio = null,
            nip5 = null,
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=${profilePubKey.value}",
            publicKey = profilePubKey.value,
        ),
        statCards = FAKE_PROFILE.statCards,
    )

    val uiState =
        userMetadataDao.observeUserMetadata(profilePubKey.value)
            .filterNotNull()
            .map {
                ProfileUiState.Loaded(
                    userNotesPagingFlow = userNotesPagingFlow,
                    userData = ProfileUiState.Loaded.UserData(
                        petName = it.displayName ?: "",
                        username = it.name ?: "",
                        bio = it.about,
                        nip5 = null,
                        avatarUrl = it.picture ?: "",
                        publicKey = it.pubkey,
                    ),
                    statCards = FAKE_PROFILE.statCards,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)

    init {
        Log.d("@@@", "pubkey: ${profilePubKey.value}")
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeMessages.forEach {
            relays.unsubscribe(it)
        }
    }
}


