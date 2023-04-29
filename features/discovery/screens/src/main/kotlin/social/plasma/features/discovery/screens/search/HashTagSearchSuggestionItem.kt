package social.plasma.features.discovery.screens.search

data class HashTagSearchSuggestionItem(
    val content: String,
    val icon: SuggestionIcon? = null,
) : SearchSuggestion {
    enum class SuggestionIcon {
        Recent,
        Popular,
    }
}
