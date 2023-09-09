package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HashtagDao {
    @Query(
        """
        SELECT h.hashtag
        FROM hashtag_search hs
        JOIN hashtag h ON hs.rowid = h.id
        JOIN hashtag_ref hr ON h.hashtag = hr.hashtag
        WHERE hashtag_search MATCH :query
        GROUP BY h.hashtag
        ORDER BY COUNT(hr.hashtag) DESC
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


    /**
     * Returns the picture for the most recent pubkeys that have used the given hashtag, in order of most recent event.
     * If the user does not, have a picture, it's skipped and the next most recent user is returned.
     */
    @Query(
        """
        WITH LatestEvent AS (
            SELECT
                events.pubkey,
                MAX(events.created_at) AS latest_date
            FROM events
            JOIN hashtag_ref ON events.id = hashtag_ref.source_event
            WHERE
                hashtag_ref.hashtag = :hashTag COLLATE NOCASE
                AND events.kind = 1
            GROUP BY events.pubkey
        )
        
        SELECT
            user_metadata.picture
        FROM user_metadata
        JOIN LatestEvent ON user_metadata.pubkey = LatestEvent.pubkey
        WHERE user_metadata.picture IS NOT NULL
        ORDER BY LatestEvent.latest_date DESC
        LIMIT :limit
    """
    )
    fun observeCommunityLatestPictures(hashTag: String, limit: Int = 5): Flow<List<String>>
}
