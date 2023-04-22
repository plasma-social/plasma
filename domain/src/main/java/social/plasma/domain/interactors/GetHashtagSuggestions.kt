package social.plasma.domain.interactors

import social.plasma.data.daos.HashtagDao
import social.plasma.domain.ResultInteractor
import javax.inject.Inject

class GetHashtagSuggestions @Inject constructor(
    private val hashtagDao: HashtagDao,
) : ResultInteractor<GetHashtagSuggestions.Params, List<String>>() {
    data class Params(val noteContent: String, val cursorPosition: Int)

    override suspend fun doWork(params: Params): List<String> {
        if (params.cursorPosition <= 0) return emptyList()

        val contentBeforeCursor = params.noteContent.substring(0, params.cursorPosition)

        val query = contentBeforeCursor.substring(contentBeforeCursor.lastIndexOf(" ").inc())
            .replace("\n", "")

        if (query.startsWith("#") && query.length > 1) {
            val recommendations =
                setOf(query) + hashtagDao.getHashTagRecommendations("${query.drop(1)}%")
                    .map { "#$it" }
            
            return recommendations.toList()
        }

        return emptyList()
    }
}

