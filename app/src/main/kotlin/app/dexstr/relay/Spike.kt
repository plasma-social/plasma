package app.dexstr.relay

import okhttp3.*
import okio.ByteString
import java.util.*

class Spike(
    private val okHttpClient: OkHttpClient,
) {

    fun doIt() {
        val request = Request.Builder()
            .url("wss://relay.damus.io")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(socket: WebSocket, response: Response) {
                println("open: $response")
                socket.send( """["REQ","testr-${UUID.randomUUID()}",{}]""")
            }

            /** Invoked when a text (type `0x1`) message has been received. */
            override fun onMessage(webSocket: WebSocket, text: String) {
                println(text)
            }

            /** Invoked when a binary (type `0x2`) message has been received. */
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println(bytes)
            }

            /**
             * Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
             */
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println(reason)
            }

            /**
             * Invoked when both peers have indicated that no more messages will be transmitted and the
             * connection has been successfully released. No further calls to this listener will be made.
             */
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println(reason)
            }

            /**
             * Invoked when a web socket has been closed due to an error reading from or writing to the
             * network. Both outgoing and incoming messages may have been lost. No further calls to this
             * listener will be made.
             */
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println(response)
                t.printStackTrace()
            }
        }

        val socket = okHttpClient.newWebSocket(request, listener)


//        val scarletInstance = Scarlet.Builder()
//            .webSocketFactory(okHttpClient.newWebSocket(request, ))
//            .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
//            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
//            .build()
    }


}

