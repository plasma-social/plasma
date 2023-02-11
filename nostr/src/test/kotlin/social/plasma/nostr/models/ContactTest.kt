package social.plasma.nostr.models

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex

class ContactTest : StringSpec({

    val pubKey = "8366029071b385def2e4fb964d2d73e6f4246131ac1ff7608bbcb1971c5081d2".decodeHex()

    "can parse from minimal tag" {
        Contact.fromTag(listOf("p", pubKey.hex())) shouldBe Contact(pubKey)
    }

    "rejects tags not starting with 'p'" {
        Contact.fromTag(listOf("x", pubKey.hex())).shouldBeNull()
    }

    "rejects tags with invalid pubKey" {
        Contact.fromTag(listOf("p", "~$pubKey")).shouldBeNull()
    }

    "rejects empty tags" {
        Contact.fromTag(emptyList()).shouldBeNull()
    }

    "rejects tags that are too short" {
        Contact.fromTag(listOf("p")).shouldBeNull()
    }

    "accepts tags in uppercase" {
        Contact.fromTag(listOf("p", pubKey.hex().uppercase())) shouldBe Contact(pubKey)
    }

    "can parse relay" {
        Contact.fromTag(listOf("p", pubKey.hex(), "wss://nostr.satsophone.tk")) shouldBe
                Contact(pubKey, "wss://nostr.satsophone.tk")
    }

    "considers relay absent if empty" {
        Contact.fromTag(listOf("p", pubKey.hex(), "")) shouldBe
                Contact(pubKey)
    }

    "can parse petname" {
        Contact.fromTag(listOf("p", pubKey.hex(), "", "satsophone")) shouldBe
                Contact(pubKey, petName = "satsophone")
    }

    "considers petname absent if empty" {
        Contact.fromTag(listOf("p", pubKey.hex(), "", "")) shouldBe
                Contact(pubKey)
    }

    "can parse both relay and petname" {
        Contact.fromTag(
            listOf("p", pubKey.hex(), "wss://nostr.satsophone.tk", "satsophone")
        ) shouldBe
                Contact(
                    pubKey = pubKey,
                    homeRelayUrl = "wss://nostr.satsophone.tk",
                    petName = "satsophone"
                )
    }

})
