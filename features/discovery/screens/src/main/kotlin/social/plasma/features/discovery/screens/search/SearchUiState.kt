package social.plasma.features.discovery.screens.search

import com.slack.circuit.CircuitUiState
import social.plasma.features.discovery.screens.search.SearchBarUiState.LeadingIcon

data class SearchUiState(
    val onEvent: (SearchUiEvent) -> Unit,
    val searchBarUiState: SearchBarUiState = SearchBarUiState(
        query = "",
        isActive = false,
        suggestionsTitle = null,
        leadingIcon = LeadingIcon.Search,
        trailingIcon = null,
        searchSuggestionGroups = emptyList()
    ),
) : CircuitUiState

data class SearchBarUiState(
    val query: String,
    val isActive: Boolean,
    val leadingIcon: LeadingIcon,
    val trailingIcon: TrailingIcon?,
    val suggestionsTitle: String?,
    val searchSuggestionGroups: List<SearchSuggestionGroup>,
) {
    enum class LeadingIcon {
        Back,
        Search,
    }

    enum class TrailingIcon {
        Clear,
    }
}

data class SearchSuggestionGroup(
    val title: String?,
    val items: List<SearchSuggestion>,
)


