package social.plasma.ui.login

sealed interface LoginState {
    object Loading : LoginState

    data class LoggedOut(
        val keyInput: String,
        val loginButtonVisible: Boolean,
        val clearInputButtonVisible: Boolean,
    ) : LoginState

    object LoggedIn : LoginState
}

