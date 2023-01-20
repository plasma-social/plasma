package social.plasma.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.take
import okio.ByteString.Companion.decodeHex
import social.plasma.relay.BuildingBlocks.JackPubKey
import social.plasma.relay.BuildingBlocks.JemPubKey
import social.plasma.relay.BuildingBlocks.client
import social.plasma.relay.BuildingBlocks.scarlet
import social.plasma.relay.Relays

class RealContactListRepositoryTest : StringSpec({

    val repo = RealContactListRepository(
        Relays(
            client,
            scarlet,
            listOf("wss://nostr.satsophone.tk"),
        )
    )

    "repository can be used to find user data" {
        repo.requestContactLists(JemPubKey)
        repo.observeContactLists().filterNot { it.isEmpty() }.take(1).collect { set ->
            set.map { contact -> contact.pubKey } shouldContain JackPubKey.decodeHex()
        }
    }
})
