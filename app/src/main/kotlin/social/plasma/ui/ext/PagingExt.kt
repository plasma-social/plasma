package social.plasma.ui.ext

import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_YEAR
import android.text.format.DateUtils.SECOND_IN_MILLIS
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import social.plasma.db.notes.NoteWithUserEntity
import social.plasma.models.PubKey
import social.plasma.ui.components.NoteCardUiModel
import java.time.Instant

fun ViewModel.noteCardsPagingFlow(
    pagingSourceFactory: () -> PagingSource<Int, NoteWithUserEntity>,
): Flow<PagingData<NoteCardUiModel>> {
    return Pager(
        config = PagingConfig(pageSize = 25),
        pagingSourceFactory = pagingSourceFactory
    ).flow.distinctUntilChanged().map { pagingData ->
        pagingData.map {
            val user = it.userMetadataEntity
            val note = it.noteEntity

            NoteCardUiModel(
                id = note.id,
                name = user?.name ?: note.pubKey,
                content = note.content,
                avatarUrl = user?.picture
                    ?: "https://api.dicebear.com/5.x/bottts/jpg?seed=${note.pubKey}",
                timePosted = Instant.ofEpochMilli(note.createdAt).relativeTime(),
                replyCount = "5",
                shareCount = "10",
                likeCount = "1323",
                userPubkey = PubKey(note.pubKey),
                nip5 = null,
            )
        }
    }.cachedIn(viewModelScope)
}

private fun Instant.relativeTime(): String {
    return DateUtils.getRelativeTimeSpanString(
        this.toEpochMilli(),
        Instant.now().toEpochMilli(),
        SECOND_IN_MILLIS,
        FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR or FORMAT_ABBREV_ALL
    ).toString()
}
