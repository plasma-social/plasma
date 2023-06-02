package social.plasma.shared.repositories.fakes

import app.cash.nostrino.crypto.SecKeyGenerator
import kotlinx.coroutines.flow.Flow
import social.plasma.shared.repositories.api.AccountStateRepository

class FakeAccountStateRepository(
    private var secretKey: ByteArray? = null,
    private var publicKey: ByteArray? = SecKeyGenerator().generate().pubKey.key.toByteArray(),
) : AccountStateRepository {

    override val isLoggedIn: Flow<Boolean>
        get() = TODO("Not yet implemented")

    override fun setSecretKey(byteArray: ByteArray) {
        secretKey = byteArray
    }

    override fun setPublicKey(byteArray: ByteArray) {
        publicKey = byteArray
    }

    override fun clearKeys() {
        secretKey = null
        publicKey = null
    }

    override fun getPublicKey(): ByteArray? = publicKey

    override fun getSecretKey(): ByteArray? = secretKey
}
