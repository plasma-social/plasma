package social.plasma.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import social.plasma.models.Event
import social.plasma.models.NoteWithUser
import social.plasma.models.events.EventEntity

private const val THREAD_DEPTH_LIMIT = 100

@Dao
interface NotesDao {
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM events WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey) 
        AND kind in (:kinds) 
        AND NOT EXISTS( SELECT 1 from event_ref WHERE source_event = events.id)
        ORDER BY created_at DESC
    """
    )
    fun observePagedContactsEvents(
        pubkey: String,
        kinds: Set<Int> = setOf(Event.Kind.Note, Event.Kind.Audio),
    ): PagingSource<Int, EventEntity>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM events WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey) AND kind in (:kinds) ORDER BY created_at DESC")
    fun observePagedContactsReplies(
        pubkey: String,
        kinds: Set<Int> = setOf(Event.Kind.Note, Event.Kind.Repost, Event.Kind.Audio),
    ): PagingSource<Int, EventEntity>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM events WHERE pubkey = :pubkey AND kind in (:kinds) ORDER BY created_at DESC")
    fun observePagedUserNotes(
        pubkey: String,
        kinds: Set<Int> = setOf(Event.Kind.Note, Event.Kind.Repost, Event.Kind.Audio),
    ): PagingSource<Int, EventEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT id FROM events
            INNER JOIN event_ref ON events.id = event_ref.source_event
            WHERE event_ref.target_event = :noteId
            AND events.pubkey = :pubkey
            AND events.kind = ${Event.Kind.Reaction}
            LIMIT 1
        )
        """
    )
    suspend fun isNoteLiked(
        pubkey: String,
        noteId: String,
    ): Boolean

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    suspend fun getById(noteId: String): NoteWithUser?

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT n.* FROM events n " +
                "LEFT JOIN pubkey_ref pr ON pr.source_event = n.id " +
                "WHERE pr.pubkey = :pubKey AND n.kind in(:kinds) ORDER BY n.created_at DESC"
    )
    fun observePagedNotifications(
        pubKey: String,
        kinds: Set<Int> = setOf(Event.Kind.Note, Event.Kind.Repost),
    ): PagingSource<Int, EventEntity>

    @Query(
        """
        WITH RECURSIVE RecursiveEvents AS (
            SELECT target_event AS event_id, 1 AS level
            FROM event_ref
            WHERE source_event = :noteId
        
            UNION ALL
        
            SELECT er.target_event, re.level + 1
            FROM event_ref er
            JOIN RecursiveEvents re ON er.source_event = re.event_id
            WHERE re.level < $THREAD_DEPTH_LIMIT  
        )

        SELECT *
        FROM events
        WHERE kind in (:kinds)
        AND (
            id IN (SELECT event_id FROM RecursiveEvents) -- notes before the selected leaf
            OR id = :noteId -- The selected leaf of the thread
            OR id IN (SELECT source_event FROM event_ref WHERE target_event = :noteId) -- notes that reply to the selected leaf
        )
        ORDER BY created_at
    """
    )
    fun observePagedThreadNotes(
        noteId: String,
        kinds: Set<Int> = setOf(Event.Kind.Note, Event.Kind.Audio),
    ): PagingSource<Int, EventEntity>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT n.* FROM noteview n WHERE content LIKE :query AND kind = ${Event.Kind.Note} AND NOT is_reply"
    )
    fun observePagedNotesWithContent(query: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM events
        INNER JOIN hashtag_ref
        ON events.id = hashtag_ref.source_event
        WHERE hashtag_ref.hashtag = :hashtagName
        AND events.kind in (:kinds)
        ORDER BY events.created_at DESC
    """
    )
    fun observePagedNotesWithHashtag(
        hashtagName: String,
        kinds: Set<Int> = setOf(Event.Kind.Note),
    ): PagingSource<Int, EventEntity>

    @Query(
        """
        SELECT COUNT(id) FROM events
        INNER JOIN hashtag_ref
        ON events.id = hashtag_ref.source_event
        WHERE hashtag_ref.hashtag = :hashtag
        AND events.created_at > :since
    """
    )
    fun observeHashTagNoteCount(hashtag: String, since: Long): Flow<Long>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    fun observeById(noteId: String): Flow<NoteWithUser?>

    @Query("SELECT * FROM events WHERE id = :noteId")
    fun observeEventById(noteId: String): Flow<EventEntity?>

    @Query(
        """
        SELECT COUNT(id) FROM events
        INNER JOIN event_ref ON events.id = event_ref.source_event
        WHERE event_ref.target_event = :noteId
        AND events.kind = ${Event.Kind.Reaction}
    """
    )
    fun observeLikeCount(noteId: String): Flow<Long>
}

