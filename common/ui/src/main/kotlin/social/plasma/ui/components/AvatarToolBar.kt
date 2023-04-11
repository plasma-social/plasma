package social.plasma.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AvatarToolBar(
    title: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    containerColor: Color = Color.Transparent,
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onAvatarClick) {
                Avatar(
                    size = 30.dp,
                    imageUrl = avatarUrl,
                    contentDescription = null,
                )
            }
        },

        )
}
