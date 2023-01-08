package social.plasma.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import social.plasma.relay.json.HexByteStringAdapter
import social.plasma.relay.json.InstantAdapter
import social.plasma.relay.json.RequestMessageAdapter

class Relays(
    private val okHttpClient: OkHttpClient,
) {

    private val moshi = Moshi.Builder()
        .add(InstantAdapter())
        .add(HexByteStringAdapter())
        .add(RequestMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun subscribe(url: String): RelayService = Scarlet.Builder()
        .webSocketFactory(okHttpClient.newWebSocketFactory(url))
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
        .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
        .build()
        .create()
}