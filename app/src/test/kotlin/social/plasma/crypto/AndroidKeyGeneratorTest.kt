package social.plasma.crypto

import fr.acinq.secp256k1.Secp256k1
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import social.plasma.models.crypto.KeyGenerator
import kotlin.random.Random

class AndroidKeyGeneratorTest {

    private val generator = KeyGenerator()

    @Test
    fun generatesValidKeys() {
        (0..100).forEach { _ ->
            val key = generator.generateKeyPair()
            val data = Random.Default.nextBytes(32)
            val sig = Secp256k1.get().signSchnorr(data, key.sec.toByteArray(), null)
            assertTrue(
                Secp256k1.get().verifySchnorr(sig, data, key.pub.toByteArray()),
                "${data.toByteString()} signed by ${key.sec} should not be ${sig.toByteString()}"
            )
        }
    }
}
