package social.plasma.features.discovery.screens.search

data class UserSearchItem(
    val pubKeyHex: String,
    val imageUrl: String?,
    val title: String,
    val nip5Identifier: String?,
    val isNip5Valid: Boolean?,
) : SearchSuggestion, SearchResult
