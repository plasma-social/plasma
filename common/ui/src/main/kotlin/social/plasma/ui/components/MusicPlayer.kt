package social.plasma.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import social.plasma.ui.R

@Composable
@SuppressLint("UnsafeOptInUsageError")
fun MusicPlayer(
    audioUrl: String,
    modifier: Modifier = Modifier,
) {
    val currentContext = LocalContext.current
    var isPlaying: Boolean by rememberSaveable { mutableStateOf(false) }
    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }

    val player: Player = remember { ExoPlayer.Builder(currentContext).build() }

    DisposableEffect(player, audioUrl) {
        player.apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            seekTo(playbackPosition)
            playWhenReady = isPlaying
            prepare()
        }
        onDispose {
            playbackPosition = player.currentPosition
            isPlaying = player.isPlaying
            player.release()
        }
    }

    ActivityLifecycleEvents { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (isPlaying) {
                    player.play()
                }
            }

            Lifecycle.Event.ON_STOP -> {
                player.pause()
            }

            else -> Unit
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.background),
        factory = { context ->
            PlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                controllerShowTimeoutMs = 1000
                defaultArtwork = AppCompatResources.getDrawable(context, R.drawable.plasma_logo)
                artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FILL
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setShutterBackgroundColor(Color.BLACK)
            }
        },
        update = {
            it.player = player
        }
    )

}
