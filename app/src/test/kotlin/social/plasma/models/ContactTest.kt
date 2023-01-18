package social.plasma.models

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex
import social.plasma.relay.BuildingBlocks.JemPubKey

class ContactTest : StringSpec({

    "can parse from minimal tag" {
        Contact.fromTag(listOf("p", JemPubKey)) shouldBe Contact(JemPubKey.decodeHex())
    }

    "rejects tags not starting with 'p'" {
        Contact.fromTag(listOf("x", JemPubKey)).shouldBeNull()
    }

    "rejects tags with invalid pubKey" {
        Contact.fromTag(listOf("p", "~$JemPubKey")).shouldBeNull()
    }

    "rejects empty tags" {
        Contact.fromTag(emptyList()).shouldBeNull()
    }

    "rejects tags that are too short" {
        Contact.fromTag(listOf("p")).shouldBeNull()
    }

    "accepts tags in uppercase" {
        Contact.fromTag(listOf("p", JemPubKey.uppercase())) shouldBe Contact(JemPubKey.decodeHex())
    }

    "can parse relay" {
        Contact.fromTag(listOf("p", JemPubKey, "wss://nostr.satsophone.tk")) shouldBe
                Contact(JemPubKey.decodeHex(), "wss://nostr.satsophone.tk")
    }

    "considers relay absent if empty" {
        Contact.fromTag(listOf("p", JemPubKey, "")) shouldBe
                Contact(JemPubKey.decodeHex())
    }

    "can parse petname" {
        Contact.fromTag(listOf("p", JemPubKey, "", "satsophone")) shouldBe
                Contact(JemPubKey.decodeHex(), petName = "satsophone")
    }

    "considers petname absent if empty" {
        Contact.fromTag(listOf("p", JemPubKey, "", "")) shouldBe
                Contact(JemPubKey.decodeHex())
    }

    "can parse both relay and petname" {
        Contact.fromTag(listOf("p", JemPubKey, "wss://nostr.satsophone.tk", "satsophone")) shouldBe
                Contact(
                    pubKey = JemPubKey.decodeHex(),
                    homeRelayUrl = "wss://nostr.satsophone.tk",
                    petName = "satsophone"
                )
    }

})
