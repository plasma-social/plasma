package social.plasma.data.daos.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import social.plasma.data.daos.HashtagDao

class FakeHashTagDao : HashtagDao {
    val hashTagRecommendations = MutableStateFlow(emptyList<String>())
    val popularHashTags = MutableStateFlow(emptyList<String>())

    override suspend fun getHashTagRecommendations(query: String, limit: Int): List<String> {
        return hashTagRecommendations.value
    }

    override suspend fun getPopularHashTags(limit: Int): List<String> {
        return popularHashTags.value
    }

    override fun observeCommunityLatestPictures(hashTag: String, limit: Int): Flow<List<String>> {
        TODO("Not yet implemented")
    }
}
