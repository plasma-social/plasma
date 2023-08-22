package social.plasma.shared.repositories.fakes

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.Tag
import social.plasma.models.events.EventEntity
import social.plasma.shared.repositories.api.NoteRepository
import java.time.Instant

class FakeNoteRepository : NoteRepository {
    val noteByIdResponse = MutableStateFlow<NoteWithUser?>(null)
    val sendNoteEvents = Turbine<SendNoteEvent>()
    override suspend fun getById(noteId: NoteId): NoteWithUser? {
        return noteByIdResponse.getAndUpdate { null }
    }

    override fun observeById(noteId: NoteId): Flow<NoteWithUser?> {
        TODO("Not yet implemented")
    }

    override fun observeEventById(noteId: NoteId): Flow<EventEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun sendNote(content: String, tags: List<Tag>) {
        sendNoteEvents.add(SendNoteEvent(content, tags))
    }

    override fun observePagedNotifications(): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override fun observePagedContactsReplies(): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshContactsNotes(): List<NoteWithUser> {
        TODO("Not yet implemented")
    }

    override suspend fun isNoteLiked(byPubKey: PubKey, noteId: NoteId): Boolean {
        return false
    }

    override fun observePagedHashTagNotes(hashtag: HashTag): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override fun observeHashTagNoteCount(hashtag: HashTag, since: Instant?): Flow<Long> {
        TODO("Not yet implemented")
    }

    override fun observePagedContactsEvents(): PagingSource<Int, EventEntity> {
        TODO("Not yet implemented")
    }

    override fun observeLikeCount(noteId: NoteId): Flow<Long> {
        TODO("Not yet implemented")
    }
}

data class SendNoteEvent(
    val content: String,
    val tags: List<Tag>,
)
