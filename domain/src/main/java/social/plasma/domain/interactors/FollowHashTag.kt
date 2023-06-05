package social.plasma.domain.interactors

import social.plasma.domain.ResultInteractor
import social.plasma.models.HashTag
import social.plasma.shared.repositories.api.ContactsRepository
import javax.inject.Inject

class FollowHashTag @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ResultInteractor<FollowHashTag.Params, FollowHashTag.FollowResult>() {
    override suspend fun doWork(params: Params): FollowResult {
        return try {
            contactsRepository.followHashTag(params.hashtag)
            FollowResult.Success
        } catch (e: Exception) {
            FollowResult.Error
        }
    }

    data class Params(
        val hashtag: HashTag,
    )

    sealed interface FollowResult {
        object Success : FollowResult
        object Error : FollowResult
    }
}

