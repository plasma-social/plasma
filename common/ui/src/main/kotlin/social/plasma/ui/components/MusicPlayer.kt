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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import social.plasma.ui.theme.PlasmaTheme

@Composable
@SuppressLint("UnsafeOptInUsageError")
fun MusicPlayer(
    audioUrl: String,
    modifier: Modifier = Modifier,
    waveform: List<Int>? = null,
) {
    val currentContext = LocalContext.current
    val player: Player =
        remember { ExoPlayer.Builder(currentContext).build() } // TODO use single instance

    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }
    var durationMillis by rememberSaveable { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            playbackPosition = player.currentPosition
            delay(100)
        }
    }

    var isAudioPlaying by remember {
        mutableStateOf(false)
    }

    DisposableEffect(player, audioUrl) {
        player.apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            seekTo(playbackPosition)
            prepare()
        }
        onDispose {
            player.release()
        }
    }

    ActivityLifecycleEvents { event ->
        when (event) {
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

                    Player.STATE_READY -> {
                        durationMillis = player.duration
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

    MusicPlayerView(
        isPlaying = isAudioPlaying,
        togglePlay = {
            if (isAudioPlaying) {
                player.pause()
            } else {
                player.play()
            }
        },
        modifier = modifier,
        waveform = waveform,
        durationMillis = durationMillis,
        playbackPosition = playbackPosition,
    )
}

@Composable
fun MusicPlayerView(
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    modifier: Modifier,
    waveform: List<Int>? = null,
    durationMillis: Long,
    playbackPosition: Long,
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
                waveform = waveform,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                durationMillis = durationMillis,
                playbackPosition = playbackPosition,
            )
        }
    }
}


@Composable
fun WaveformGraph(
    modifier: Modifier = Modifier,
    waveform: List<Int>,
    durationMillis: Long,
    playbackPosition: Long,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    unPlayedColor: Color = Color.LightGray,
) {
    require(waveform.isNotEmpty()) { "Waveform cannot be empty" }
    require(playbackPosition >= 0) { "Playback position ($playbackPosition) cannot be negative" }
    require(durationMillis >= 0) { "Duration ($durationMillis) cannot be negative" }

    if (durationMillis == 0L) return

    val cornerRadius = with(LocalDensity.current) { CornerRadius(2.dp.toPx()) }

    val maxAmplitude = (waveform.maxOrNull() ?: 1).toFloat()
    val playbackProgress = (playbackPosition.toFloat() / durationMillis).coerceIn(0f, 1f)

    Canvas(modifier) {
        val drawYStartPoint = size.height / 2
        val barAndGapWidth = size.width / waveform.size
        val barWidth = barAndGapWidth * (2f / 3f)

        for (i in waveform.indices) {
            val barHeight = (waveform[i] / maxAmplitude) * size.height
            val fractionOfWaveform = i.toFloat() / waveform.size

            val isCurrentBarBeingPlayed =
                fractionOfWaveform < playbackProgress && (fractionOfWaveform + 1f / waveform.size) > playbackProgress
            val playedRatio = when {
                isCurrentBarBeingPlayed -> (playbackProgress - fractionOfWaveform) * waveform.size
                fractionOfWaveform < playbackProgress -> 1f
                else -> 0f
            }

            if (fractionOfWaveform <= playbackProgress) {
                val playedWidth = barWidth * playedRatio

                if (playedRatio < 1f) {

                    Path().apply {
                        val rect = Rect(
                            offset = Offset(
                                x = i * barAndGapWidth,
                                y = drawYStartPoint - barHeight / 2
                            ),
                            size = Size(width = playedWidth, height = barHeight),
                        )

                        val roundRect = RoundRect(
                            rect = rect,
                            topLeft = cornerRadius,
                            topRight = CornerRadius.Zero,
                            bottomLeft = cornerRadius,
                            bottomRight = CornerRadius.Zero,
                        )

                        addRoundRect(roundRect)
                        drawPath(this, playedColor)
                    }

                    Path().apply {
                        val rect = Rect(
                            offset = Offset(
                                x = i * barAndGapWidth + playedWidth,
                                y = drawYStartPoint - barHeight / 2
                            ),
                            size = Size(width = barWidth - playedWidth, height = barHeight),
                        )

                        val roundRect = RoundRect(
                            rect = rect,
                            topLeft = CornerRadius.Zero,
                            topRight = cornerRadius,
                            bottomLeft = CornerRadius.Zero,
                            bottomRight = cornerRadius,
                        )

                        addRoundRect(roundRect)
                        drawPath(this, unPlayedColor)
                    }
                } else {
                    drawRoundRect(
                        color = playedColor,
                        topLeft = Offset(
                            x = i * barAndGapWidth,
                            y = drawYStartPoint - barHeight / 2
                        ),
                        size = Size(width = barWidth, height = barHeight),
                        cornerRadius = cornerRadius,
                    )
                }
            } else {
                // The bar hasn't been reached yet, draw it with the start color
                drawRoundRect(
                    color = unPlayedColor,
                    topLeft = Offset(x = i * barAndGapWidth, y = drawYStartPoint - barHeight / 2),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = cornerRadius,
                )
            }
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
            durationMillis = 5000,
            playbackPosition = 2000,
        )
    }
}

@Preview
@Composable
fun WaveformGraphPreview() {

    WaveformGraph(
        waveform = waveform,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(6.5f)
            .padding(4.dp),
        durationMillis = 100,
        playbackPosition = 51,
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
