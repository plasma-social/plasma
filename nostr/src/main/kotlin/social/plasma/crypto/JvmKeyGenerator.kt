package social.plasma.crypto

import okio.ByteString.Companion.toByteString
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class JvmKeyGenerator : KeyGenerator {

    override fun generateKey(): KeyPair {
        val bs = ByteArray(32)
        SecureRandom().nextBytes(bs)
        // TODO exclude values too high https://crypto.stackexchange.com/a/72739
        return KeyPair(
            Bech32.pubkeyCreate(bs).toByteString(),
            bs.toByteString()
        )

/*
        val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
        val ecs = ECGenParameterSpec("secp256k1")
        keyGen.initialize(ecs, SecureRandom())
        val pair: java.security.KeyPair = keyGen.genKeyPair()
        val pub = pair.public as ECPublicKey

        return KeyPair(
            EC5Util.convertPoint(pub.params, pub.w)
                .getEncoded(true).toByteString(),
            (pair.private as ECPrivateKey).s.toByteArray().toByteString()
        )
*/
    }

}