package social.plasma.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.relay.message.Filters
import social.plasma.relay.message.NostrMessageAdapter
import java.time.Instant

class RelayTest : StringSpec({

    // TODO - a deterministic test using a mock webserver
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
        val relay = relays.relay("wss://brb.io").connectAndSubscribe(
            filters = Filters(
                since = Instant.EPOCH,
                authors = setOf("8366029071b385def2e4fb964d2d73e6f4246131ac1ff7608bbcb1971c5081d2"),
                kinds = setOf(3),
            )
        )
        val events = relay.flowRelayMessages().take(1).toList()
        events.forEach { println(it) }
        events shouldHaveSize 1
    }
})