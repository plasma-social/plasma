package social.plasma.relay

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.*
import kotlin.coroutines.CoroutineContext

class RelayWebSocketListener(
    private val sink: MutableSharedFlow<String>,
    private val coroutineContext: CoroutineContext
) : WebSocketListener() {

    private val subscriptionId = "plasma-sub-${UUID.randomUUID()}"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("""["REQ","$subscriptionId",{}]""")
    }

    /** Invoked when a text (type `0x1`) message has been received. */
    override fun onMessage(webSocket: WebSocket, text: String) {
        runBlocking(context = coroutineContext) { sink.emit(text) }
    }

    /** Invoked when a binary (type `0x2`) message has been received. */
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        runBlocking(context = coroutineContext) { sink.emit(bytes.base64()) }
    }

    /**
     * Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
     */
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(javaClass.simpleName, "closing: $reason")
    }

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the
     * connection has been successfully released. No further calls to this listener will be made.
     */
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(javaClass.simpleName, "closed: $reason")
    }

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the
     * network. Both outgoing and incoming messages may have been lost. No further calls to this
     * listener will be made.
     */
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(javaClass.simpleName, "websocket failure", t)
    }
}