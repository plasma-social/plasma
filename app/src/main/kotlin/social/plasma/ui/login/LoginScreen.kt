package social.plasma.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import social.plasma.R
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginState.LoggedOut,
    onKeyInputChanged: (String) -> Unit,
    onLoginButtonClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Surface(modifier = modifier.safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Image(
                modifier = Modifier.shadow(
                    shape = CircleShape,
                    elevation = 48.dp,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary,
                ),
                painter = painterResource(id = R.drawable.plasma_logo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.displaySmall,
            )

            Text(text = stringResource(R.string.login_tagline))

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.private_or_public_key)) },
                keyboardActions = KeyboardActions(onGo = { onLoginButtonClick() }),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Go,
                    capitalization = KeyboardCapitalization.None,
                ),
                supportingText = { Text(stringResource(R.string.sign_in_with_a_public_or_secret_key)) },
                trailingIcon = if (uiState.clearInputButtonVisible) {
                    {
                        IconButton(onClick = { onKeyInputChanged("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                value = uiState.keyInput,
                onValueChange = onKeyInputChanged,
            )
            AnimatedVisibility(
                modifier = Modifier,
                visible = uiState.loginButtonVisible,
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    onClick = onLoginButtonClick,
                ) {
                    Text(stringResource(id = R.string.login))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewLoginScreen() {
    PlasmaTheme {
        LoginScreen(
            uiState = LoginState.LoggedOut(
                keyInput = "",
                loginButtonVisible = true,
                clearInputButtonVisible = true,
            ),
            onLoginButtonClick = {},
            onKeyInputChanged = {},
        )
    }
}
