package social.plasma.ui.components.notes

import social.plasma.PubKey

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

        data class Text(val text: String) : ContentBlock

        data class UrlPreview(val url: String) : ContentBlock

        sealed class Mention : ContentBlock {
            abstract val text: String
        }

        data class ProfileMention(override val text: String, val pubkey: String) : Mention()

        data class NoteMention(override val text: String, val id: String) : Mention()
    }
}
