package social.plasma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import social.plasma.R

@Composable
fun Avatar(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null,
) {
    AsyncImage(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .error(R.drawable.avatar_fallback)
            .placeholder(R.drawable.avatar_fallback)
            .fallback(R.drawable.avatar_fallback)
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = contentDescription
    )
}

@Composable
fun ZoomableAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    ZoomableImage(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        imageUrl = imageUrl,
        contentScale = ContentScale.Crop,
        options = {
            crossfade(true)
            error(R.drawable.avatar_fallback)
            placeholder(R.drawable.avatar_fallback)
            fallback(R.drawable.avatar_fallback)
        }
    )
}
