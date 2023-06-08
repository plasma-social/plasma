package social.plasma.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import social.plasma.models.Event
import social.plasma.models.NoteWithUser

@Dao
interface NotesDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey) AND NOT is_reply")
    fun observePagedContactsNotes(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey IN (SELECT pubkey FROM contacts WHERE owner = :pubkey)")
    fun observePagedContactsReplies(pubkey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE pubkey = :pubkey")
    fun observePagedUserNotes(pubkey: String): PagingSource<Int, NoteWithUser>

    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM event_ref " +
                "LEFT JOIN events on event_ref.source_event = events.id " +
                "WHERE pubkey = :pubkey AND kind = 7 AND target_event = :noteId " +
                "GROUP BY event_ref.source_event )"

    )
    suspend fun isNoteLiked(pubkey: String, noteId: String): Boolean

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM noteview WHERE id = :noteId")
    suspend fun getById(noteId: String): NoteWithUser?

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT n.* FROM noteview n " +
                "LEFT JOIN pubkey_ref pr ON pr.source_event = n.id " +
                "WHERE pr.pubkey = :pubKey ORDER BY n.created_at DESC"
    )
    fun observePagedNotifications(pubKey: String): PagingSource<Int, NoteWithUser>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM noteview WHERE kind = ${Event.Kind.Note} AND (" +
                "id in(SELECT source_event FROM event_ref WHERE target_event = :noteId) " +
                "OR id = :noteId " +
                "OR id in(SELECT target_event FROM event_ref WHERE source_event = :noteId)" +
                ") ORDER BY created_at"
    )
    fun observePagedThreadNotes(noteId: String): PagingSource<Int, NoteWithUser>

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
        SELECT * FROM noteview
        INNER JOIN hashtag_ref
        ON noteview.id = hashtag_ref.source_event
        WHERE hashtag_ref.hashtag = :hashtagName
        ORDER BY noteview.created_at DESC
    """
    )
    fun observePagedNotesWithHashtag(hashtagName: String): PagingSource<Int, NoteWithUser>

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
}
