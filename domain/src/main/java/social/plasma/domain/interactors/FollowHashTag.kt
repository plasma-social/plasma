package social.plasma.domain.interactors

import social.plasma.domain.ResultInteractor
import social.plasma.shared.repositories.api.ContactsRepository
import javax.inject.Inject

class FollowHashTag @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ResultInteractor<FollowHashTag.Params, FollowHashTag.FollowResult>() {
    override suspend fun doWork(params: Params): FollowResult {
        val hashtag = if (params.hashtag.startsWith("#")) params.hashtag.drop(1) else params.hashtag
        return try {
            contactsRepository.followHashTag(hashtag)
            FollowResult.Success
        } catch (e: Exception) {
            FollowResult.Error
        }
    }

    data class Params(
        val hashtag: String,
    )

    sealed interface FollowResult {
        object Success : FollowResult
        object Error : FollowResult
    }
}

