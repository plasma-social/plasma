package social.plasma.ui.components.notes

import social.plasma.PubKey

data class NoteUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val nip5: String?,
    val content: String,
    val cardLabel: String?,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: String,
    val userPubkey: PubKey,
    val richContent: RichContent = RichContent.None,
) {
    sealed interface RichContent {
        object None : RichContent
        data class Image(val imageUrl: String) : RichContent

        data class Carousel(val imageUrls: List<String>) : RichContent
    }
}