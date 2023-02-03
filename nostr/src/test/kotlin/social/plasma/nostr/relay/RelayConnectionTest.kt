package social.plasma.nostr.relay

import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.nostr.BuildingBlocks
import social.plasma.nostr.BuildingBlocks.JemPubKey
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter

@OptIn(ExperimentalCoroutinesApi::class)
class RelayConnectionTest : StringSpec({

    val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE))
        .build()


    "can get events from a relay" {
/*
        runTest {
            val server = LocalRelay(7070).start()
            val relay = RelayImpl(
                server.url,
                BuildingBlocks.scarlet
                    .webSocketFactory(client.newWebSocketFactory(server.url))
                    .build()
                    .create(),
                this
            )
            relay.connect()
            val events = relay.subscribe(
                ClientMessage.SubscribeMessage(Filter.contactList(JemPubKey))
            )
            events.first().event.content shouldContain "nostr.satsophone.tk"
            relay.disconnect()
            server.stop()
        }
*/
    }

})