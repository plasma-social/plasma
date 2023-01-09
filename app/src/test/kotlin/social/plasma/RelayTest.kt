package social.plasma

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.relay.Relays

class RelayTest : StringSpec({

    "can get events from a relay" {
        val relays = Relays(OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()
        )
        val relay = relays.relay("wss://relay.damus.io").connectAndSubscribe()
        val events = relay.flowNotes().take(3).toList()
        events.forEach { println(it) }
        events shouldHaveSize 3
    }
})