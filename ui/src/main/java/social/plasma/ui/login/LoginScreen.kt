package social.plasma.ui.login

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import social.plasma.ui.R
import social.plasma.ui.components.PrimaryButton
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginState.LoggedOut,
    onKeyInputChanged: (String) -> Unit,
    onLoginButtonClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

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
                PrimaryButton(
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

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoginScreen() {
    PlasmaTheme(darkTheme = true) {
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
