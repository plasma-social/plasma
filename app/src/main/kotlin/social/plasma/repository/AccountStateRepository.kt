package social.plasma.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import social.plasma.crypto.Bech32
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.models.PubKey
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.prefs.Preference
import javax.inject.Inject

interface AccountStateRepository {
    val isLoggedIn: Flow<Boolean>

    fun setSecretKey(byteArray: ByteArray)

    fun setPublicKey(byteArray: ByteArray)

    fun clearKeys()

    fun getPublicKey(): ByteArray?

    fun getSecretKey(): ByteArray?
    fun syncMyData(): Flow<Unit>
}

class RealAccountRepository @Inject constructor(
    @UserKey(KeyType.Secret)
    private val secretKey: Preference<ByteArray>,
    @UserKey(KeyType.Public)
    private val publicKey: Preference<ByteArray>,
    private val relay: Relay,
    private val contactListRepository: ContactListRepository,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun syncMyData(): Flow<Unit> {
        // TODO change this from a flow
        val myPubkey = PubKey.of(publicKey.get(null)!!).hex
        return contactListRepository.syncContactList(myPubkey).distinctUntilChanged()
            .filterNot { it.isEmpty() }.flatMapLatest {
                val contactNpubList = it.map { it.pubKey.hex() }.toSet()

                relay.subscribe(
                    SubscribeMessage(
                        filter = Filter(authors = setOf(myPubkey), limit = 1000),
                        Filter(
                            authors = contactNpubList,
                            kinds = setOf(Event.Kind.Repost, Event.Kind.Note)
                        ),
                        Filter(pTags = setOf(myPubkey)),
                    )
                )
            }.map { }
    }
}
