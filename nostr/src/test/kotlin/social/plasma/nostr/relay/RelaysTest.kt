package social.plasma.nostr.relay

import io.kotest.core.spec.style.StringSpec

class RelaysTest : StringSpec({

/*
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
*/
})
