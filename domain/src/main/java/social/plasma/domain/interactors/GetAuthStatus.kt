package social.plasma.domain.interactors

import social.plasma.domain.ResultInteractor
import social.plasma.domain.interactors.AuthStatus.Authenticated
import social.plasma.domain.interactors.AuthStatus.ReadOnly
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject

class GetAuthStatus @Inject constructor(
    private val accountStateRepository: AccountStateRepository,
) : ResultInteractor<Unit, AuthStatus?>() {
    override suspend fun doWork(params: Unit): AuthStatus? {
        return when {
            accountStateRepository.getSecretKey() != null -> Authenticated
            accountStateRepository.getPublicKey() != null -> ReadOnly
            else -> null
        }
    }
}

sealed interface AuthStatus {
    object ReadOnly : AuthStatus

    object Authenticated: AuthStatus
}
