package social.plasma.relay

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request

class Relays(
    private val okHttpClient: OkHttpClient,
) {

    suspend fun subscribe(
        url: String,
    ): Flow<String> {
        val messages = MutableSharedFlow<String>()
        okHttpClient.newWebSocket(
            request = Request.Builder().url(url).build(),
            listener = RelayWebSocketListener(messages, currentCoroutineContext())
        )
        return messages
    }

}