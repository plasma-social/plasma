package social.plasma.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.video.VideoSize

@Composable
fun InlineMediaPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    var aspectRatio: Float by rememberSaveable { mutableStateOf(1f) }

    var isPlaying: Boolean by rememberSaveable { mutableStateOf(false) }

    var player: Player? by remember { mutableStateOf(null) }

    var playbackPosition by rememberSaveable { mutableStateOf(0L) }

    val currentContext = LocalContext.current

    LaunchedEffect(Unit) {
        player = ExoPlayer.Builder(currentContext).build().apply {
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(size: VideoSize) {
                    super.onVideoSizeChanged(size)
                    aspectRatio = size.width / size.height.toFloat()
                }
            })

            setMediaItem(MediaItem.fromUri(videoUrl))
            seekTo(playbackPosition)
            playWhenReady = isPlaying
            prepare()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        factory = { context ->
            StyledPlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                controllerShowTimeoutMs = 1000
            }
        },
        update = {
            it.player = player
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            player?.let { exoPlayer ->
                playbackPosition = exoPlayer.currentPosition
                isPlaying = exoPlayer.isPlaying
                exoPlayer.release()
            }
            player = null
        }
    }
}
