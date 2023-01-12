package social.plasma.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
    Surface {
        ConstraintLayout(
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
            val (input, button, title) = createRefs()
            Column(
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top)
                    bottom.linkTo(input.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .constrainAs(input) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                keyboardActions = KeyboardActions(onGo = { onLoginButtonClick() }),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Go
                ),
                singleLine = true,
                value = uiState.keyInput,
                placeholder = { Text(stringResource(R.string.private_or_public_key)) },
                onValueChange = onKeyInputChanged,
            )

            AnimatedVisibility(
                modifier = Modifier.constrainAs(button) {
                    top.linkTo(input.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                visible = uiState.loginButtonVisible,
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
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
            ),
            onLoginButtonClick = {},
            onKeyInputChanged = {},
        )
    }
}
