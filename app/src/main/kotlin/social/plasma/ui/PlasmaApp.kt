package social.plasma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.login.LoginScreen
import social.plasma.ui.login.LoginState
import social.plasma.ui.login.LoginViewModel
import social.plasma.ui.main.MainScreen

@Composable
fun PlasmaApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by loginViewModel.uiState.collectAsState()

    when (uiState) {
        LoginState.Loading -> ProgressIndicator()
        LoginState.LoggedIn -> MainScreen(
            modifier = modifier,
            navController = navController,
        )

        is LoginState.LoggedOut -> LoginScreen(
            uiState = uiState as LoginState.LoggedOut,
            onKeyInputChanged = loginViewModel::onKeyChanged,
            onLoginButtonClick = loginViewModel::login
        )
    }
}
