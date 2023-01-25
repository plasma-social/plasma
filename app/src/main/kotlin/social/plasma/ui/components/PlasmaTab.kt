package social.plasma.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PlasmaTab(
    useLeftRoundShape: Boolean,
    useRightRoundShape: Boolean,
    selected: Boolean,
    @StringRes title: Int,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(
        topStart = if (useLeftRoundShape) 24.dp else 0.dp,
        bottomStart = if (useLeftRoundShape) 24.dp else 0.dp,
        topEnd = if (useRightRoundShape) 24.dp else 0.dp,
        bottomEnd = if (useRightRoundShape) 24.dp else 0.dp,
    )

    val backgroundColor =
        if (selected.not()) MaterialTheme.colorScheme.background else Color.Transparent

    Tab(modifier = Modifier
        .clip(shape)
        .background(color = backgroundColor)
        .border(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant,
            shape = shape
        ),
        selected = selected,
        onClick = onClick,
        selectedContentColor = MaterialTheme.colorScheme.onSurface,
        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),

        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(visible = selected) {
                    Icon(
                        painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = stringResource(title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

        })
}
