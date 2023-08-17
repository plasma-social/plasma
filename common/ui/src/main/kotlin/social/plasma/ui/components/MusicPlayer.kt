package social.plasma.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import social.plasma.ui.theme.PlasmaTheme

@Composable
@SuppressLint("UnsafeOptInUsageError")
fun MusicPlayer(
    audioUrl: String,
    modifier: Modifier = Modifier,
    waveform: List<Int>? = null,
) {
    val currentContext = LocalContext.current
    var shouldAutoplay: Boolean by rememberSaveable { mutableStateOf(false) }
    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }
    var isAudioPlaying by remember {
        mutableStateOf(false)
    }

    val player: Player =
        remember { ExoPlayer.Builder(currentContext).build() } // TODO use single instance

    DisposableEffect(player, audioUrl) {
        player.apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            seekTo(playbackPosition)
            playWhenReady = shouldAutoplay
            prepare()
        }
        onDispose {
            playbackPosition = player.currentPosition
            shouldAutoplay = player.isPlaying
            player.release()
        }
    }

    ActivityLifecycleEvents { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (shouldAutoplay) {
                    player.play()
                }
            }

            Lifecycle.Event.ON_STOP -> {
                player.pause()
            }

            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isAudioPlaying = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        player.seekTo(0)
                        player.pause()
                    }

                    else -> Unit
                }
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
        }
    }

    MusicPlayerView(isAudioPlaying, togglePlay = {
        if (isAudioPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }, modifier, waveform)
}

@Composable
fun MusicPlayerView(
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    modifier: Modifier,
    waveform: List<Int>? = null,
) {
    val playIcon = rememberVectorPainter(image = Icons.Default.PlayCircle)
    val pauseIcon = rememberVectorPainter(image = Icons.Default.PauseCircle)

    Row(
        modifier.aspectRatio(6.5f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crossfade(targetState = isPlaying, label = "playButton") { playing ->
            IconButton(onClick = togglePlay) {
                Image(
                    if (playing) pauseIcon else playIcon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(48.dp)
                )
            }

        }

        waveform?.let {
            WaveformGraph(
                waveform = waveform, modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}


@Composable
fun WaveformGraph(waveform: List<Int>, modifier: Modifier = Modifier) {
    val maxAmplitude = waveform.maxOrNull() ?: 1
    val color = MaterialTheme.colorScheme.primary

    Canvas(modifier) {
        val barWidth = size.width / (waveform.size * 1.5f)  // 1 bar + 0.5 bar gap
        val horizontalGap = barWidth / 2

        for (i in waveform.indices) {
            val barHeight = (waveform[i] / maxAmplitude.toFloat()) * size.height / 2
            val xOffset = (i * (barWidth + horizontalGap)) + (barWidth / 2)
            val start = Offset(x = xOffset, y = size.height / 2 - barHeight)
            val end = Offset(x = xOffset, y = size.height / 2 + barHeight)
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview
@Composable
fun MusicPlayerPreview() {
    PlasmaTheme {
        MusicPlayerView(
            isPlaying = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            togglePlay = {},
            waveform = waveform,
        )
    }
}

@Preview
@Composable
fun WaveformGraphPreview() {

    WaveformGraph(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(6.5f)
            .padding(4.dp),
        waveform = waveform,
    )
}

private val waveform = listOf(
    13,
    19,
    52,
    23,
    50,
    45,
    36,
    23,
    65,
    38,
    29,
    48,
    53,
    27,
    53,
    38,
    14,
    17,
    22,
    27,
    17,
    32,
    21,
    9,
    40,
    17,
    33,
    13,
    9,
    10,
    15,
    28,
    12,
    16,
    39,
    18,
    21,
    4,
    38,
    22,
    19,
    1,
    28,
    34,
    17,
    22,
    22,
    22,
    43,
    20,
    15,
    13,
    41,
    24,
    12,
    14,
    7,
    13,
    8,
    10,
    9,
    9,
    9
)
