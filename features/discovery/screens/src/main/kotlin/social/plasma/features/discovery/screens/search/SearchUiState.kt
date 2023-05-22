package social.plasma.features.discovery.screens.search

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.discovery.screens.search.SearchBarUiState.LeadingIcon

data class SearchUiState(
    val onEvent: (SearchUiEvent) -> Unit,
    val searchBarUiState: SearchBarUiState = SearchBarUiState(
        query = "",
        isActive = false,
        suggestionsTitle = null,
        leadingIcon = LeadingIcon.Search,
        trailingIcon = SearchBarUiState.TrailingIcon.Clear,
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

    sealed interface TrailingIcon {
        object Clear : TrailingIcon
        data class Avatar(val url: String?) : TrailingIcon
    }
}

data class SearchSuggestionGroup(
    val title: String?,
    val items: List<SearchSuggestion>,
)


