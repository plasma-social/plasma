package social.plasma.crypto

import okio.ByteString.Companion.toByteString
import java.security.SecureRandom

class AndroidKeyGenerator : KeyGenerator {

    override fun generateKey(): KeyPair {
        val bs = ByteArray(32)
        SecureRandom().nextBytes(bs)
        // TODO exclude values too high https://crypto.stackexchange.com/a/72739
        return KeyPair(
            Bech32.pubkeyCreate(bs).toByteString(),
            bs.toByteString()
        )
    }

}