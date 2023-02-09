package social.plasma.nostr.relay

import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.crypto.KeyGenerator
import social.plasma.crypto.KeyPair
import social.plasma.nostr.BuildingBlocks.JemPubKey
import social.plasma.nostr.BuildingBlocks.moshi
import social.plasma.nostr.BuildingBlocks.scarlet
import social.plasma.nostr.BuildingBlocks.testRelay
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import java.time.Instant

class RealRelayTest : StringSpec({

    "can write notes to a relay" {
        val relay = testRelay(this)
        val text = Arb.stringPattern("[a-zA-Z0-9]+").next()
        relay.connect()
        val key = KeyGenerator().generateKey()
        val notes = relay.subscribe(SubscribeMessage(Filter.userNotes(pubKey = key.pub.hex())))
        val message = EventMessage(
            Event.createEvent(
                key.pub,
                key.sec,
                Instant.now(),
                Event.Kind.Note,
                emptyList(), // TODO - send the right tags
                text
            )
        )

        relay.send(message)
        with(notes.first().event) {
            content shouldBe text
            pubKey shouldBe key.pub
        }
        relay.disconnect()
    }
})
