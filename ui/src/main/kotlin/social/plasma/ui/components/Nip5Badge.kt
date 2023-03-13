package social.plasma.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import social.plasma.ui.R

@Composable
fun Nip5Badge(
    identifier: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_plasma_verified),
            // TODO tint with correct color
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
        )
        Text(
            identifier,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
