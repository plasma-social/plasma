package social.plasma.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import social.plasma.models.PubKey
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val profilePubKey: PubKey = PubKey(checkNotNull(savedStateHandle["pubkey"]))
    private val _uiState = MutableStateFlow(FAKE_PROFILE)

    val uiState: StateFlow<ProfileUiState>
        get() = _uiState.asStateFlow()
}
