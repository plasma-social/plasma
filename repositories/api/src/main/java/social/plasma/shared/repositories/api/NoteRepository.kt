package social.plasma.shared.repositories.api

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.Tag
import java.time.Instant

interface NoteRepository {
    suspend fun getById(noteId: NoteId): NoteWithUser?

    suspend fun sendNote(content: String, tags: List<Tag>)

    fun observePagedContactsNotes(): PagingSource<Int, NoteWithUser>

    fun observePagedNotifications(): PagingSource<Int, NoteWithUser>

    fun observePagedContactsReplies(): PagingSource<Int, NoteWithUser>

    fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, NoteWithUser>

    fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, NoteWithUser>

    suspend fun refreshContactsNotes(): List<NoteWithUser>
    suspend fun isNoteLiked(byPubKey: PubKey, noteId: NoteId): Boolean
    fun observePagedNotesWithContent(hashtag: HashTag): PagingSource<Int, NoteWithUser>

    fun observePagedHashTagNotes(hashtag: HashTag): PagingSource<Int, NoteWithUser>
    fun observeHashTagNoteCount(hashtag: HashTag, since: Instant?): Flow<Long>
}
