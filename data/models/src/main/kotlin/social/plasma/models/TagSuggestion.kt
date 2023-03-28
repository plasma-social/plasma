package social.plasma.models

data class TagSuggestion(
    val pubKey: PubKey,
    val imageUrl: String?,
    val title: String,
    val subtitle: String?,
)
