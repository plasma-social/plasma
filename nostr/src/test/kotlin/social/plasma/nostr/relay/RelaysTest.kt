package social.plasma.nostr.relay

import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import social.plasma.nostr.BuildingBlocks.JemPubKey
import social.plasma.nostr.BuildingBlocks.client
import social.plasma.nostr.BuildingBlocks.scarlet
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import kotlin.time.Duration.Companion.seconds

class RelaysTest : StringSpec({

    fun relays(): Relays = Relays(
        okHttpClient = client,
        scarletBuilder = scarlet,
        relayUrlList = listOf("wss://brb.io", "wss://eden.nostr.land", "wss://relay.snort.social")
    )

    "can get notes from relay" {
        relays().subscribe(SubscribeMessage(Filter.userNotes(JemPubKey)))
            .first().event.pubKey.hex() shouldBe JemPubKey
    }

    "multiple subscribers get the same messages".config(enabled = false) {
        relays().subscribe(SubscribeMessage(Filter.userMetaData(JemPubKey)))
            .test(timeout = 5.seconds) {
                awaitItem().event.pubKey.hex() shouldBe JemPubKey
                awaitItem().event.pubKey.hex() shouldBe JemPubKey
                awaitItem().event.pubKey.hex() shouldBe JemPubKey
                ensureAllEventsConsumed()
            }
    }
})
