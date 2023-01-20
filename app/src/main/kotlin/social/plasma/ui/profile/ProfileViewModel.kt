package social.plasma.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import social.plasma.models.PubKey
import social.plasma.repository.NoteRepository
import social.plasma.repository.RealUserMetaDataRepository
import social.plasma.ui.ext.noteCardsPagingFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    userMetaDataRepository: RealUserMetaDataRepository,
) : ViewModel() {
    private val fakeProfile =
        ProfilePreviewProvider().values.filterIsInstance(ProfileUiState.Loaded::class.java).first()

    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))

    private val userNotesPagingFlow =
        noteCardsPagingFlow(noteRepository.observeProfileNotes(profilePubKey.value))

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
        userMetaDataRepository.observeUserMetaData(profilePubKey.value)
            .filterNotNull()
            .map {
                ProfileUiState.Loaded(
                    userNotesPagingFlow = userNotesPagingFlow,
                    userData = ProfileUiState.Loaded.UserData(
                        petName = it.displayName ?: profilePubKey.shortBech32,
                        username = it.name?.let { "@$it" },
                        about = it.about,
                        nip5 = null,
                        avatarUrl = it.picture ?: "",
                        publicKey = profilePubKey,
                    ),
                    statCards = fakeProfile.statCards,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState)


    fun onNoteDisposed(id: String) {
        // TODO cancel coroutine
    }

    fun onNoteDisplayed(id: String) {
        // TODO  move to repo
        noteRepository.observeNoteReactionCount(id).launchIn(viewModelScope)
    }
}

