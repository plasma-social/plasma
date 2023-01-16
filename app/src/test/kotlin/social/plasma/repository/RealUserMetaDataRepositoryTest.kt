package social.plasma.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.take
import okio.ByteString.Companion.decodeHex
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.moshi
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner

class RealUserMetaDataRepositoryTest : StringSpec({

    val repo = RealUserMetaDataRepository(
        Relays(client, scarlet, listOf("wss://brb.io")),
        EventRefiner(moshi)
    )

    "repository can be used to find user data" {
        // TODO - have this return subscriptions that can be used to unsubscribe
        repo.requestUserMetaData(JemPubKey)
        repo.observeGlobalUserMetaData().filterNot { it.isEmpty() }.take(1).collect {
            it.first().pubKey shouldBe JemPubKey.decodeHex()
        }
    }

})
