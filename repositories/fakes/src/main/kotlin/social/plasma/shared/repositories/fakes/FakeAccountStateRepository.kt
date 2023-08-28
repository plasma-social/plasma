package social.plasma.shared.repositories.fakes

import app.cash.nostrino.crypto.SecKeyGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import social.plasma.models.crypto.Bech32
import social.plasma.shared.repositories.api.AccountStateRepository
import java.time.Instant

class FakeAccountStateRepository(
    private var secretKey: ByteArray? = null,
    private var publicKey: ByteArray? = SecKeyGenerator().generate().pubKey.key.toByteArray(),
) : AccountStateRepository {

    override val isLoggedIn: Flow<Boolean>
        get() = TODO("Not yet implemented")

    override fun setSecretKey(byteArray: ByteArray) {
        secretKey = byteArray
        publicKey = Bech32.pubkeyCreate(byteArray)
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
    override fun observeLastNotificationSeenTimestamp(): Flow<Instant> {
        return emptyFlow()
    }

    override fun updateLastNotificationSeen() {
        TODO("Not yet implemented")
    }
}
