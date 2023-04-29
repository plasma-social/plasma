package social.plasma.domain.interactors

import social.plasma.data.daos.HashtagDao
import social.plasma.domain.ResultInteractor
import javax.inject.Inject

class GetHashtagSuggestions @Inject constructor(
    private val hashtagDao: HashtagDao,
) : ResultInteractor<GetHashtagSuggestions.Params, List<String>>() {
    data class Params(val query: String)

    override suspend fun doWork(params: Params): List<String> {
        val query = if (params.query.startsWith("#")) {
            params.query.substring(1)
        } else {
            params.query
        }

        if (query.isEmpty()) {
            return emptyList()
        }

        return mutableSetOf<String>().apply {
            if (params.query.startsWith("#")) {
                add(query)
            }

            addAll(hashtagDao.getHashTagRecommendations("$query%"))
        }.toList()
    }
}

