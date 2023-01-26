package social.plasma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collect
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
    appViewModel: AppViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        appViewModel.syncGlobalData.collect()
    }

    val loginState by loginViewModel.uiState.collectAsState()

    when (loginState) {
        LoginState.Loading -> ProgressIndicator()
        LoginState.LoggedIn -> MainScreen(
            modifier = modifier,
            navController = navController,
        )

        is LoginState.LoggedOut -> LoginScreen(
            uiState = loginState as LoginState.LoggedOut,
            onKeyInputChanged = loginViewModel::onKeyChanged,
            onLoginButtonClick = loginViewModel::login
        )
    }
}
