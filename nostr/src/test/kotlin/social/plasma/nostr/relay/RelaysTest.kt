package social.plasma.nostr.relay

import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import social.plasma.nostr.BuildingBlocks.JemPubKey
import social.plasma.nostr.BuildingBlocks.client
import social.plasma.nostr.BuildingBlocks.scarlet
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.SubscribeMessage
import kotlin.time.Duration.Companion.seconds

class RelaysTest : StringSpec({

    fun relays(): Relays = Relays(
        okHttpClient = client,
        scarletBuilder = scarlet,
        relayUrlList = listOf("wss://brb.io")
    )

    "can get metadata from relay" {
        // TODO - have this return subscriptions that can be used to unsubscribe
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
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))),
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))),
            relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey))),
        ).merge().test(timeout = 5.seconds) {
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            awaitItem().event.pubKey.hex() shouldBe JemPubKey
            ensureAllEventsConsumed()
        }
    }
})
