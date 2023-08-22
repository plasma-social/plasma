package social.plasma.shared.repositories.api

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.Tag
import social.plasma.models.events.EventEntity
import java.time.Instant

interface NoteRepository {
    suspend fun getById(noteId: NoteId): NoteWithUser?

    fun observeById(noteId: NoteId): Flow<NoteWithUser?>

    fun observeEventById(noteId: NoteId): Flow<EventEntity?>

    suspend fun sendNote(content: String, tags: List<Tag>)

    fun observePagedNotifications(): PagingSource<Int, EventEntity>

    fun observePagedContactsReplies(): PagingSource<Int, EventEntity>

    fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, EventEntity>

    fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, EventEntity>

    suspend fun refreshContactsNotes(): List<NoteWithUser>
    suspend fun isNoteLiked(byPubKey: PubKey, noteId: NoteId): Boolean

    fun observePagedHashTagNotes(hashtag: HashTag): PagingSource<Int, EventEntity>
    fun observeHashTagNoteCount(hashtag: HashTag, since: Instant?): Flow<Long>

    fun observePagedContactsEvents(): PagingSource<Int, EventEntity>
    fun observeLikeCount(noteId: NoteId): Flow<Long>
}

