package social.plasma.features.onboarding.ui.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.onboarding.screens.login.LoginUiEvent.OnClearInput
import social.plasma.features.onboarding.screens.login.LoginUiEvent.OnInputChange
import social.plasma.features.onboarding.screens.login.LoginUiEvent.OnLogin
import social.plasma.features.onboarding.screens.login.LoginUiState
import social.plasma.ui.R
import social.plasma.ui.components.PrimaryButton
import social.plasma.ui.theme.PlasmaTheme

class LoginScreenUi : Ui<LoginUiState> {
    @Composable
    override fun Content(state: LoginUiState, modifier: Modifier) {
        val scrollState = rememberScrollState()
        val onEvent = state.onEvent

        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Surface(modifier = modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                Image(
                    modifier = Modifier
                        .size(75.dp),
                    painter = painterResource(id = R.drawable.plasma_logo),
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                )

                Text(text = stringResource(R.string.login_tagline), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(40.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.private_or_public_key)) },
                    keyboardActions = KeyboardActions(onGo = { onEvent(OnLogin) }),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = ImeAction.Go,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    supportingText = { Text(stringResource(R.string.sign_in_with_a_public_or_secret_key)) },
                    trailingIcon = if (state.clearInputButtonVisible) {
                        {
                            IconButton(onClick = { onEvent(OnClearInput) }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    value = state.keyInput,
                    onValueChange = { onEvent(OnInputChange(it)) },
                )
                AnimatedVisibility(
                    modifier = Modifier,
                    visible = state.loginButtonVisible,
                ) {
                    PrimaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        onClick = { onEvent(OnLogin) },
                    ) {
                        Text(stringResource(id = R.string.login))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoginScreen() {
    PlasmaTheme(darkTheme = true) {
        LoginScreenUi().Content(
            modifier = Modifier,
            state = LoginUiState(
                keyInput = "test",
                loginButtonVisible = true,
                clearInputButtonVisible = true,
                onEvent = {}
            )
        )
    }
}
