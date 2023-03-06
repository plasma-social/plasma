package social.plasma.db.reactions

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class ReactionDao {
    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM event_ref " +
                "LEFT JOIN events on event_ref.source_event = events.id " +
                "WHERE pubkey = :pubkey AND kind = 7 AND target_event = :noteId " +
                "GROUP BY event_ref.source_event )"

    )
    abstract suspend fun isNoteLiked(pubkey: String, noteId: String): Boolean
}
