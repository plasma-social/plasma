package social.plasma.domain.interactors

import social.plasma.domain.ResultInteractor
import social.plasma.shared.repositories.api.ContactsRepository
import javax.inject.Inject

class FollowPubkey @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ResultInteractor<FollowPubkey.Params, FollowPubkey.FollowResult>() {
    override suspend fun doWork(params: Params): FollowResult {
        return try {
            contactsRepository.followPubkey(params.pubKeyHex)
            FollowResult.Success
        } catch (e: Exception) {
            FollowResult.Error
        }
    }

    data class Params(
        val pubKeyHex: String,
    )

    sealed interface FollowResult {
        object Success : FollowResult
        object Error : FollowResult
    }
}

