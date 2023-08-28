package social.plasma.shared.repositories.real

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import social.plasma.models.crypto.Bech32
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.utils.api.Preference
import social.plasma.shared.utils.real.prefs.LongPreference
import java.time.Instant
import javax.inject.Inject

internal class RealAccountRepository @Inject constructor(
    @UserKey(KeyType.Secret)
    private val secretKey: Preference<ByteArray>,
    @UserKey(KeyType.Public)
    private val publicKey: Preference<ByteArray>,
    private val longPreferenceFactory: LongPreference.LongPreferenceFactory,
) : AccountStateRepository {
    private val _isLoggedIn = MutableStateFlow(secretKey.isSet() || publicKey.isSet())
    private val lastNotificationSeen: LongPreference =
        longPreferenceFactory.create("last_notification_seen")

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

    override fun observeLastNotificationSeenTimestamp(): Flow<Instant> =
        lastNotificationSeen.observe().map { Instant.ofEpochMilli(it ?: 0) }

    override fun updateLastNotificationSeen() {
        lastNotificationSeen.set(Instant.now().toEpochMilli())
    }

}
