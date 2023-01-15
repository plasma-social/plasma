package social.plasma.repository

import fakes.FakeNoteDao
import fakes.FakeUserMetadataDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.moshi
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage

class RelaysTest : StringSpec({
    val metadataDao = FakeUserMetadataDao()
    val noteDao = FakeNoteDao()

    val relays = Relays(
        okHttpClient = client,
        eventRefiner = EventRefiner(moshi),
        noteDao = noteDao,
        userMetadataDao = metadataDao,
        scarletBuilder = scarlet,
        relayUrlList = listOf("wss://brb.io")
    )

    "user metadata gets saved to db" {
        // TODO - have this return subscriptions that can be used to unsubscribe
        relays.subscribe(SubscribeMessage(filters = Filters.userMetaData(JemPubKey)))

        metadataDao.inserts.awaitItem().pubkey shouldBe JemPubKey
    }

    "notes get saved to db" {
        relays.subscribe(SubscribeMessage(filters = Filters.userNotes(JemPubKey)))

        noteDao.inserts.awaitItem().pubKey shouldBe JemPubKey
    }
})
