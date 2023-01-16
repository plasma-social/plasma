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
import social.plasma.crypto.Bech32.bechToBytes
import social.plasma.repository.AccountStateRepository
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountStateRepo: AccountStateRepository,
    @Named("default")
    defaultDispatcher: CoroutineContext,
) : ViewModel() {
    private val keyInput: MutableStateFlow<String> = MutableStateFlow("")
    private val decodedKey = keyInput.map { bech32 ->
        try {
            bech32.bechToBytes()
        } catch (e: IllegalArgumentException) {
            null
        }
    }.flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<LoginState> =
        combine(keyInput, decodedKey, accountStateRepo.isLoggedIn) { key, decodedKey, loggedIn ->
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
            val decodedKey = decodedKey.value ?: return@launch

            if (keyInput.value.startsWith("npub")) {
                accountStateRepo.setPublicKey(decodedKey)
            } else if (keyInput.value.startsWith("nsec")) {
                accountStateRepo.setSecretKey(decodedKey)
            }
        }
    }
}
