package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import okio.ByteString.Companion.toByteString
import social.plasma.domain.ResultInteractor
import social.plasma.domain.interactors.AuthStatus.Authenticated
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject

class GetAuthStatus @Inject constructor(
    private val accountStateRepository: AccountStateRepository,
) : ResultInteractor<Unit, AuthStatus?>() {
    override suspend fun doWork(params: Unit): AuthStatus? {
        return when {
            accountStateRepository.getSecretKey() != null -> Authenticated.Write(
                PubKey(
                    accountStateRepository.getPublicKey()!!.toByteString()
                )
            )

            accountStateRepository.getPublicKey() != null -> Authenticated.ReadOnly(
                PubKey(
                    accountStateRepository.getPublicKey()!!.toByteString()
                )
            )

            else -> null
        }
    }
}

sealed interface AuthStatus {
    sealed interface Authenticated : AuthStatus {
        val pubkey: PubKey

        data class ReadOnly(override val pubkey: PubKey) : Authenticated

        data class Write(override val pubkey: PubKey) : Authenticated
    }
}
