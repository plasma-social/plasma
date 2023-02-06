package social.plasma.ui.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.acinq.secp256k1.Secp256k1
import junit.framework.TestCase.assertTrue
import okio.ByteString.Companion.toByteString
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.crypto.AndroidKeyGenerator
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class AndroidKeyGeneratorTest {

    private val generator = AndroidKeyGenerator()

    @Test
    fun generatesValidKeys() {
        (0..100).forEach { _ ->
            val key = generator.generateKey()
            val data = Random.Default.nextBytes(32)
            val sig = Secp256k1.get().signSchnorr(data, key.sec.toByteArray(), null)
            assertTrue(
                "${data.toByteString()} signed by ${key.sec} should not be ${sig.toByteString()}",
                Secp256k1.get().verifySchnorr(sig, data, key.pub.toByteArray())
            )
        }
    }
}