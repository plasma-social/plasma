package social.plasma.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.relay.message.NostrMessageAdapter

class RelayTest : StringSpec({

    "can get events from a relay" {
        val moshi = Moshi.Builder()
            .add(NostrMessageAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val relays = Relays(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build(),
            Scarlet.Builder()
                .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        )
        val relay = relays.relay("wss://relay.damus.io").connectAndSubscribe()
        val events = relay.flowRelayMessages().take(3).toList()
        events.forEach { println(it) }
        events shouldHaveSize 3
    }
})