package social.plasma.crypto

import fr.acinq.secp256k1.Secp256k1
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import okio.ByteString.Companion.toByteString

class KeyGeneratorTest : StringSpec({

    "generates valid keys" {
        checkAll(arbTestData) { (key, data) ->
            val hash = data.toByteArray(Charsets.UTF_8).toByteString().sha256().toByteArray()
            val sig = Secp256k1.get().signSchnorr(hash, key.sec.toByteArray(), null)
            Secp256k1.get().verifySchnorr(sig, hash, key.pub.toByteArray()) shouldBe true
        }
    }

}) {
    companion object {
        private val arbKey =
            arbitrary { social.plasma.models.crypto.KeyGenerator().generateKeyPair() }
        val arbTestData = Arb.pair(arbKey, Arb.string())
    }
}
