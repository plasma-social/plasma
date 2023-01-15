package social.plasma.repository

import fakes.FakeNoteDao
import fakes.FakeUserMetadataDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.take
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.moshi
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner

class RealUserMetaDataRepositoryTest : StringSpec({

    val userMetadataDao = FakeUserMetadataDao()
    val repo = RealUserMetaDataRepository(
        Relays(
            client, scarlet, listOf("wss://brb.io"), FakeNoteDao(),
            userMetadataDao,
            EventRefiner(
                moshi
            )
        ),
        userMetadataDao
    )

    "repository can be used to find user data" {
        // TODO - have this return subscriptions that can be used to unsubscribe
        repo.requestUserMetaData(JemPubKey)
        repo.observeUserMetaData(JemPubKey).take(1).collect {
            it.pubkey shouldBe JemPubKey
        }
    }

})
