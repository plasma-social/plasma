package social.plasma.domain.interactors

import social.plasma.data.daos.HashtagDao
import social.plasma.domain.ResultInteractor
import javax.inject.Inject

class GetPopularHashTags @Inject constructor(
    private val hashtagDao: HashtagDao,
) : ResultInteractor<GetPopularHashTags.Params, List<String>>() {
    data class Params(val limit: Int)

    override suspend fun doWork(params: Params): List<String> {
        return hashtagDao.getPopularHashTags(limit = params.limit)
    }
}

