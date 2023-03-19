package social.plasma.shared.repositories.api

import kotlinx.coroutines.flow.Flow

interface AccountStateRepository {
    val isLoggedIn: Flow<Boolean>

    fun setSecretKey(byteArray: ByteArray)

    fun setPublicKey(byteArray: ByteArray)

    fun clearKeys()

    fun getPublicKey(): ByteArray?

    fun getSecretKey(): ByteArray?
}