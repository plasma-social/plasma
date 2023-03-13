package social.plasma.ui.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import social.plasma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post(
    state: PostUiState,
    onBackClicked: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onPostNote: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.new_note),
) {

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val input = remember { mutableStateOf(TextFieldValue()) }

    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        painterResource(id = R.drawable.ic_chevron_back),
                        contentDescription = null,
                    )
                }
            },
            actions = {
                AnimatedVisibility(
                    visible = state.postEnabled,
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Button(onClick = { onPostNote(input.value.text) }) {
                        Text(stringResource(R.string.post))
                    }
                }
            }
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .focusRequester(focusRequester)
                .padding(horizontal = 16.dp),
            value = input.value,
            onValueChange = { newValue ->
                if (newValue != input.value) {
                    input.value = newValue
                    onNoteChanged(newValue.text)
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = { Text(stringResource(id = R.string.your_message)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrect = true,
                keyboardType = KeyboardType.Ascii,
            ),
        )
    }
}
