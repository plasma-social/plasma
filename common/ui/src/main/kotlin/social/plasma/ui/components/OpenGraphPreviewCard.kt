package social.plasma.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import social.plasma.opengraph.OpenGraphMetadata
typealias  GetOpenGraphMetadata = suspend (String) -> OpenGraphMetadata?

@Composable
fun OpenGraphPreviewCard(
    url: String,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var openGraphMetadata by remember { mutableStateOf<OpenGraphMetadata?>(null) }

    LaunchedEffect(url) {
        openGraphMetadata = getOpenGraphMetadata(url)
    }

    openGraphMetadata?.let { metadata ->
        OutlinedCard(
            modifier = modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        ) {
            Column {
                metadata.image?.let {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        contentScale = ContentScale.Crop,
                        model = it,
                        contentDescription = null,
                    )
                    Divider()
                }
                ListItem(
                    headlineContent = {
                        metadata.title?.let {
                            Text(it)
                        }
                    },
                    supportingContent = {
                        metadata.description?.let {
                            Text(
                                it,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    overlineContent = {
                        metadata.siteName?.let {
                            Text(it)
                        }
                    }
                )
            }
        }
    }
}
