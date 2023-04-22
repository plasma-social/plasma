package social.plasma.features.posting.presenters

import social.plasma.data.daos.HashtagDao

// TODO - Move to a common module
class FakeHashTagDao : HashtagDao {
    override suspend fun getHashTagRecommendations(query: String, limit: Int): List<String> {
        TODO("Not yet implemented")
    }

}
