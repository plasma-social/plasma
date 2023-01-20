package social.plasma.repository

import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage

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
