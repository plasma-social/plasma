package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import social.plasma.crypto.Bech32
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.prefs.Preference
import javax.inject.Inject

interface AccountStateRepository {
    val isLoggedIn: Flow<Boolean>

    fun setSecretKey(byteArray: ByteArray)

    fun setPublicKey(byteArray: ByteArray)

    fun clearKeys()

    fun getPublicKey(): ByteArray?
    
    fun getSecretKey(): ByteArray?
}

class RealAccountRepository @Inject constructor(
    @UserKey(KeyType.Secret)
    private val secretKey: Preference<ByteArray>,
    @UserKey(KeyType.Public)
    private val publicKey: Preference<ByteArray>,
) : AccountStateRepository {
    private val _isLoggedIn = MutableStateFlow(secretKey.isSet() || publicKey.isSet())

    override val isLoggedIn: Flow<Boolean>
        get() = _isLoggedIn.asStateFlow()

    override fun setSecretKey(byteArray: ByteArray) {
        secretKey.set(byteArray)
        publicKey.set(Bech32.pubkeyCreate(byteArray))
        _isLoggedIn.compareAndSet(expect = false, update = true)
    }

    override fun setPublicKey(byteArray: ByteArray) {
        publicKey.set(byteArray)
        secretKey.remove()
        _isLoggedIn.compareAndSet(expect = false, update = true)
    }

    override fun clearKeys() {
        secretKey.remove()
        publicKey.remove()
        _isLoggedIn.compareAndSet(expect = true, update = false)
    }

    override fun getPublicKey(): ByteArray? {
        return publicKey.get(default = null)
    }

    override fun getSecretKey(): ByteArray? {
        return secretKey.get(default = null)
    }

}
