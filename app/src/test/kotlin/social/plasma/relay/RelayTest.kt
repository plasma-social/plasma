package social.plasma.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.NostrMessageAdapter

class RelayTest : StringSpec({

    // TODO - a deterministic test using a mock webserver
    "can get events from a relay" {
        val relays = Relays(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build(),
            Scarlet.Builder()
                .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
                .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                .addStreamAdapterFactory(CoroutinesStreamAdapterFactory()),
            EventRefiner(moshi)
        )
        val relay = relays.relay("wss://brb.io").connectAndSubscribe(
            filters = Filters.contactList("8366029071b385def2e4fb964d2d73e6f4246131ac1ff7608bbcb1971c5081d2")
        )

        val events = relay.flowRelayMessages().take(1).toList()
        events.first().event.content shouldContain "nostr.satsophone.tk"

        relay.userMeta("67e4027f797f15c8a89de7a03a07ddb0efe63985fa6716f8b3c742a008ca0be7")
            .take(1).toList() shouldHaveSize 1
    }
}) {
    companion object {
        val moshi = Moshi.Builder()
            .add(NostrMessageAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
}