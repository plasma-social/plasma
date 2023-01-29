package social.plasma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZoomableImage(imageUrl: String, modifier: Modifier = Modifier) {

    var showFullImage by remember { mutableStateOf(false) }

    AsyncImage(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { showFullImage = true },
        model = imageUrl,
        contentScale = ContentScale.FillWidth,
        contentDescription = null
    )
    
    if (showFullImage) {
        var offset by remember { mutableStateOf(Offset.Zero) }
        var zoom by remember { mutableStateOf(1f) }

        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    modifier = modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { centroid, pan, gestureZoom, _ ->
                                    val oldScale = zoom
                                    val newScale = zoom * gestureZoom
                                    offset =
                                        (offset + centroid / oldScale) -
                                                (centroid / newScale + pan / oldScale)
                                    zoom = newScale
                                }
                            )
                        }
                        .graphicsLayer {
                            translationX = -offset.x * zoom
                            translationY = -offset.y * zoom
                            scaleX = zoom
                            scaleY = zoom
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                    model = imageUrl,
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )

                FilledIconButton(
                    onClick = { showFullImage = false },
                    modifier = Modifier.align(Alignment.TopEnd),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.scrim
                    )
                ) {
                    Icon(Icons.Default.Close, null)
                }
            }
        }
    }


}
