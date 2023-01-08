package social.plasma.relay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.CoroutineContext

class Relays(
    private val okHttpClient: OkHttpClient,
    private val coroutineContext: CoroutineContext,
) {

    fun subscribe(
        url: String,
    ): Flow<String> {
        val messages = MutableSharedFlow<String>()
        okHttpClient.newWebSocket(
            request = Request.Builder().url(url).build(),
            listener = RelayWebSocketListener(messages, coroutineContext)
        )
        return messages
    }

}