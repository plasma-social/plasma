package social.plasma.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

private suspend fun <T> getChunk(channel: Channel<T>, maxChunkSize: Int): List<T> {
    val received = channel.receive()
    val chunk = mutableListOf(received)
    while (chunk.size < maxChunkSize) {
        val polled = channel.tryReceive().getOrNull()
            ?: return chunk

        chunk.add(polled)
    }
    return chunk
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.chunked(maxSize: Int, checkIntervalMillis: Long = 0): Flow<List<T>> {
    val buffer = Channel<T>(maxSize)
    return channelFlow {
        coroutineScope {
            launch {
                this@chunked.collect {
                    // `send` will suspend if [maxSize] elements are currently in buffer
                    buffer.send(it)
                }
                buffer.close()
            }
            launch {
                while (!buffer.isClosedForReceive) {
                    val chunk = getChunk(buffer, maxSize)
                    this@channelFlow.send(chunk)
                    delay(checkIntervalMillis)
                }
            }
        }

    }
}
