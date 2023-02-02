package social.plasma.ui.mappers

import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_YEAR
import android.text.format.DateUtils.SECOND_IN_MILLIS
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import social.plasma.PubKey
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.ui.components.notes.NoteUiModel
import social.plasma.ui.components.notes.NoteUiModel.RichContent
import java.time.Instant
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class NoteCardsMapper @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) {
    fun map(pagingDataFlow: Flow<PagingData<NoteWithUser>>): Flow<PagingData<NoteUiModel>> {
        return pagingDataFlow.distinctUntilChanged().map { pagingData ->
            pagingData.map { note ->
                val pubKeyTags = mutableSetOf<PubKey>()

                note.noteEntity.tags.forEach {
                    if (it.firstOrNull() == "p") {
                        val pubkey = PubKey(it[1])
                        if (pubkey.hex != note.noteEntity.pubkey) {
                            pubKeyTags.add(pubkey)
                        }
                    }
                }

                val users = pubKeyTags.map { pubkey ->
                    userMetadataDao.getById(pubkey.hex).firstOrNull()?.name
                        ?: pubkey.shortBech32
                }

                val bannerLabel = if (users.isNotEmpty()) {
                    users.generateBannerLabel()
                } else null

                note.toNoteUiModel(bannerLabel = bannerLabel)
            }
        }.flowOn(ioDispatcher)
    }

    private fun Iterable<String>.generateBannerLabel(): String {
        return "Replying to ${this.joinToString()}"
    }
}

private val imageUrlRegex by lazy { Pattern.compile("https?:/(/[^/]+)+\\.(?:jpg|gif|png|jpeg|svg)") }

fun NoteWithUser.toNoteUiModel(
    bannerLabel: String? = null,
): NoteUiModel {
    val note = noteEntity
    val user = userMetadataEntity
    val userPubkey = PubKey(note.pubkey)
    val imageUrls = note.parseImageUrls()

    return NoteUiModel(
        id = note.id,
        name = user?.name ?: userPubkey.shortBech32,
        content = note.content,
        avatarUrl = user?.picture
            ?: "https://api.dicebear.com/5.x/bottts/jpg?seed=${userPubkey.hex}",
        timePosted = Instant.ofEpochMilli(note.createdAt).relativeTime(),
        replyCount = "",
        shareCount = "",
        likeCount = "${if (note.reactionCount > 0) note.reactionCount else ""}",
        userPubkey = userPubkey,
        nip5 = user?.nip05,
        displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.name
        ?: userPubkey.shortBech32,
        richContent = when {
            imageUrls.size == 1 -> RichContent.Image(imageUrls.first())
            imageUrls.size > 1 -> RichContent.Carousel(imageUrls)
            else -> RichContent.None
        },
        cardLabel = bannerLabel,
    )
}

private fun NoteView.parseImageUrls(): List<String> {
    val matcher = imageUrlRegex.matcher(content)
    val urls = mutableSetOf<String>()

    while (matcher.find()) {
        urls.add(matcher.group())
    }

    return urls.toList()
}


private fun Instant.relativeTime(): String {
    return DateUtils.getRelativeTimeSpanString(
        this.toEpochMilli(),
        Instant.now().toEpochMilli(),
        SECOND_IN_MILLIS,
        FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR or FORMAT_ABBREV_ALL
    ).toString()
}