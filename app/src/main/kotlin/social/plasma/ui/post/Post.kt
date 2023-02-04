package social.plasma.ui.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.plasma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post(
    state: PostUiState,
    onBackClicked: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onPostNote: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.new_note)) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        painterResource(id = R.drawable.ic_chevron_back),
                        contentDescription = null,
                    )
                }
            },
        )

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.drop_a_note),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        
        val input = remember { mutableStateOf(TextFieldValue()) }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            value = input.value,
            onValueChange = { newValue ->
                if (newValue != input.value) {
                    input.value = newValue
                    onNoteChanged(newValue.text)
                }
            },
            label = { Text(stringResource(id = R.string.your_message))}
        )

        AnimatedVisibility(
            visible = state.postEnabled,
            modifier = Modifier.align(alignment = Alignment.End),
        ) {
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onPostNote(input.value.text) },
            ) {
                Text(text = stringResource(id = R.string.post_note))
            }
        }
    }
}