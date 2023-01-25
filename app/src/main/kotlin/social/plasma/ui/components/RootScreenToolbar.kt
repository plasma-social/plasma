package social.plasma.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import social.plasma.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RootScreenToolbar() {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.feed)) },
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Avatar(
                    size = 30.dp,
                    imageUrl = "https://api.dicebear.com/5.x/bottts/jpg",
                    contentDescription = null,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}
