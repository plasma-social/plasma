package social.plasma.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import social.plasma.prefs.UserKeyPreference
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userKeyPref: UserKeyPreference,
) : ViewModel() {
    private val keyInput: MutableStateFlow<String> = MutableStateFlow("")
    private val isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(userKeyPref.isSet())

    val uiState: StateFlow<LoginState> = combine(keyInput, isLoggedIn) { keyInput, loggedIn ->
        if (loggedIn) {
            LoginState.LoggedIn
        } else {
            LoginState.LoggedOut(
                keyInput = keyInput,
                loginButtonVisible = keyInput.isNotBlank(),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LoginState.Loading)

    fun onKeyChanged(value: String) {
        keyInput.tryEmit(value)
    }

    fun login() {
        viewModelScope.launch {
            // TODO check that key is a valid pub or priv key
            userKeyPref.set(keyInput.value)
            isLoggedIn.emit(userKeyPref.isSet())
        }
    }
}
