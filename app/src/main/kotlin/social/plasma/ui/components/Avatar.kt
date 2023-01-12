package social.plasma.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Avatar(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape),
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            // TODO .error()
            // TODO .placeholder()
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = contentDescription
    )
}