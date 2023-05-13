package social.plasma.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AvatarToolBar(
    title: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        actions = actions,
        navigationIcon = {
            IconButton(onClick = onAvatarClick) {
                Avatar(
                    size = 30.dp,
                    imageUrl = avatarUrl,
                    contentDescription = null,
                )
            }
        }
    )
}
