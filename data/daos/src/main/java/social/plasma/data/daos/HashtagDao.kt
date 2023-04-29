package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface HashtagDao {
    @Query(
        """
        SELECT DISTINCT hashtag FROM hashtag_ref
        WHERE hashtag LIKE LOWER(:query)
        GROUP BY hashtag
        ORDER BY COUNT(hashtag) DESC
        LIMIT :limit
    """
    )
    suspend fun getHashTagRecommendations(query: String, limit: Int = 10): List<String>

    @Query(
        """
        SELECT DISTINCT hashtag FROM hashtag_ref
        GROUP BY hashtag
        ORDER BY COUNT(hashtag) DESC
        LIMIT :limit
    """
    )
    suspend fun getPopularHashTags(limit: Int = 10): List<String>
}
