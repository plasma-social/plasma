package social.plasma.ui.ext

import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_YEAR
import android.text.format.DateUtils.SECOND_IN_MILLIS
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import social.plasma.PubKey
import social.plasma.db.notes.NoteWithUser
import social.plasma.ui.components.NoteCardUiModel
import java.time.Instant

fun ViewModel.noteCardsPagingFlow(
    pagingFlow: Flow<PagingData<NoteWithUser>>,
): Flow<PagingData<NoteCardUiModel>> {
    return pagingFlow.distinctUntilChanged().map { pagingData ->
        pagingData.map {
            val note = it.noteEntity
            val user = it.userMetadataEntity
            val userPubkey = PubKey(note.pubkey)

            NoteCardUiModel(
                id = note.id,
                name = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.name
                ?: userPubkey.shortBech32,
                content = note.content,
                avatarUrl = user?.picture
                    ?: "https://api.dicebear.com/5.x/bottts/jpg?seed=${userPubkey.hex}",
                timePosted = Instant.ofEpochMilli(note.createdAt).relativeTime(),
                replyCount = "",
                shareCount = "",
                likeCount = "${if (note.reactionCount > 0) note.reactionCount else ""}",
                userPubkey = userPubkey,
                nip5 = user?.nip05,
                displayName = user?.displayName
            )
        }
    }.distinctUntilChanged().cachedIn(viewModelScope)
}

private fun Instant.relativeTime(): String {
    return DateUtils.getRelativeTimeSpanString(
        this.toEpochMilli(),
        Instant.now().toEpochMilli(),
        SECOND_IN_MILLIS,
        FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR or FORMAT_ABBREV_ALL
    ).toString()
}
