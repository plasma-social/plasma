package social.plasma.ui.components.notes

import social.plasma.models.PubKey
import social.plasma.ui.components.richtext.Mention

data class NoteUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val nip5: String?,
    val content: List<ContentBlock>,
    val cardLabel: String?,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: Int,
    val userPubkey: PubKey,
    val isLiked: Boolean = false,
) {
    sealed interface ContentBlock {

        data class Image(val imageUrl: String) : ContentBlock

        data class Video(val videoUrl: String) : ContentBlock

        data class Carousel(val imageUrls: List<String>) : ContentBlock

        data class Text(val content: String, val mentions: Map<Int, Mention>) : ContentBlock

        data class UrlPreview(val url: String) : ContentBlock
    }
}
