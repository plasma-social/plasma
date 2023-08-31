package social.plasma.features.discovery.screens.search

import social.plasma.models.Nip5Status

data class UserSearchItem(
    val pubKeyHex: String,
    val imageUrl: String?,
    val title: String,
    val nip5Status: Nip5Status,
) : SearchSuggestion, SearchResult
