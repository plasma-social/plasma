package social.plasma.nostr.relay

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern
import kotlinx.coroutines.flow.first
import social.plasma.models.crypto.KeyGenerator
import social.plasma.nostr.BuildingBlocks.testRelay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter

class RealRelayTest : StringSpec({

    "can write notes and subscribe to a relay" {
        val relay = testRelay(this)
        val text = Arb.stringPattern("[a-zA-Z0-9]+").next()

        relay.connect()
        val keyPair = social.plasma.models.crypto.KeyGenerator().generateKeyPair()
        val notes = relay.subscribe(SubscribeMessage(Filter.userNotes(pubKey = keyPair.pub.hex())))
        relay.sendNote(text, keyPair)

        with(notes.first().event) {
            content shouldBe text
            pubKey shouldBe keyPair.pub
        }

        relay.disconnect()
    }
})
