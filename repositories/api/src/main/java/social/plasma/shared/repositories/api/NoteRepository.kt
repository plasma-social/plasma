package social.plasma.shared.repositories.api

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.Tag

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
    fun observePagedNotesWithContent(hashtag: String): PagingSource<Int, NoteWithUser>
    
    fun observePagedHashTagNotes(hashtag: String): PagingSource<Int, NoteWithUser>
}
