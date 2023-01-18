package social.plasma.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.take
import okio.ByteString.Companion.decodeHex
import social.plasma.relay.BuildingBlocks.JackPubKey
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.moshi
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner

class RealContactListRepositoryTest : StringSpec({

    val repo = RealContactListRepository(
        Relays(client, scarlet, listOf("wss://nostr.satsophone.tk")),
        EventRefiner(moshi)
    )

    "repository can be used to find user data" {
        repo.requestContactLists(JemPubKey)
        repo.observeContactLists().filterNot { it.isEmpty() }.take(1).collect {
            it.first().content.should { set ->
                set.map { contact -> contact.pubKey } shouldContain JackPubKey.decodeHex()
            }
        }
    }

})
