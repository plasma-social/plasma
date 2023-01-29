package social.plasma.nostr.relay

import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import social.plasma.nostr.BuildingBlocks.JemPubKey
import social.plasma.nostr.BuildingBlocks.client
import social.plasma.nostr.BuildingBlocks.scarlet
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.SubscribeMessage

class RelaysTest : StringSpec({

    fun relays(): Relays = Relays(
        okHttpClient = client,
        scarletBuilder = scarlet,
        relayUrlList = listOf("wss://brb.io")
    )

    "can get metadata from relay" {
        relays().subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey)))
            .first().event.pubKey.hex() shouldBe JemPubKey
    }

    "can get notes from relay" {
        relays().subscribe(SubscribeMessage(filters = Filters.userNotes(JemPubKey)))
            .first().event.pubKey.hex() shouldBe JemPubKey
    }

    "multiple subscribers get the same messages" {
        val relays = relays()

        listOf(
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))).take(1),
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))).take(1),
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))).take(1),
        ).merge().test {
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            ensureAllEventsConsumed()
        }
    }
})
