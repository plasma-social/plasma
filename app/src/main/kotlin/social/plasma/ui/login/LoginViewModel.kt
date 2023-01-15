package social.plasma.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.komputing.kbech32.AddressFormatException
import org.komputing.kbech32.Bech32
import social.plasma.di.UserKey
import social.plasma.prefs.Preference
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


@HiltViewModel
class LoginViewModel @Inject constructor(
    @UserKey
    private val userKeyPref: Preference<String>,
    @Named("default")
    defaultDispatcher: CoroutineContext,
) : ViewModel() {
    private val keyInput: MutableStateFlow<String> = MutableStateFlow("")
    private val isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(userKeyPref.isSet())
    private val decodedKey = keyInput.map { bech32 ->
        try {
            Bech32.decode(bech32)
        } catch (e: AddressFormatException) {
            null
        }
    }.flowOn(defaultDispatcher)

    val uiState: StateFlow<LoginState> =
        combine(keyInput, decodedKey, isLoggedIn) { key, decodedKey, loggedIn ->
            if (loggedIn) {
                LoginState.LoggedIn
            } else {
                LoginState.LoggedOut(
                    keyInput = key,
                    loginButtonVisible = decodedKey != null,
                    clearInputButtonVisible = key.isNotEmpty(),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginState.Loading)


    fun onKeyChanged(value: String) {
        keyInput.tryEmit(value)
    }

    fun login() {
        viewModelScope.launch {
            userKeyPref.set(keyInput.value)
            isLoggedIn.emit(userKeyPref.isSet())
        }
    }
}
