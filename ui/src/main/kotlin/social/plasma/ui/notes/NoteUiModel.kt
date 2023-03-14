package social.plasma.ui.notes

import social.plasma.models.PubKey
import social.plasma.ui.components.richtext.Mention

data class NoteUiModel(
    val id: String,
    val key: String = id,
    val name: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val nip5Identifier: String? = null,
    val headerContent: ContentBlock.Text? = null,
    val content: List<ContentBlock> = emptyList(),
    val cardLabel: String? = null,
    val timePosted: String = "",
    val replyCount: String = "",
    val shareCount: String = "",
    val likeCount: Int = 0,
    val userPubkey: PubKey,
    val hidden: Boolean = false,
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
