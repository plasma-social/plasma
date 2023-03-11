package social.plasma.ui.notes

import social.plasma.models.PubKey
import social.plasma.ui.components.richtext.Mention

data class NoteUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val nip5Identifier: String?,
    val content: List<ContentBlock>,
    val cardLabel: String?,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: Int,
    val userPubkey: PubKey,
    val isLiked: Boolean = false,
    val isNip5Valid: suspend (PubKey, String?) -> Boolean = { _, _ -> false },
    val nip5Domain: String? = null,
) {
    sealed interface ContentBlock {

        data class Image(val imageUrl: String) : ContentBlock

        data class Video(val videoUrl: String) : ContentBlock

        data class Carousel(val imageUrls: List<String>) : ContentBlock

        data class Text(val content: String, val mentions: Map<Int, Mention>) : ContentBlock

        data class UrlPreview(val url: String) : ContentBlock
    }
}
