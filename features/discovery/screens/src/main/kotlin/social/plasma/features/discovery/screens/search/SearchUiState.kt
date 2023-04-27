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
        suggestions = emptyList()
    ),
) : CircuitUiState

data class SearchBarUiState(
    val query: String,
    val isActive: Boolean,
    val leadingIcon: LeadingIcon,
    val trailingIcon: TrailingIcon?,
    val suggestionsTitle: String?,
    val suggestions: List<Suggestion>,
) {
    enum class LeadingIcon {
        Back,
        Search,
    }

    enum class TrailingIcon {
        Clear,
    }
}


sealed interface Suggestion {
    val content: String
    val icon: SuggestionIcon?

    enum class SuggestionIcon {
        Recent,
    }

    data class UserSuggestion(
        override val content: String,
        override val icon: SuggestionIcon? = null,
    ) : Suggestion

    data class CommunitySuggestion(
        override val content: String,
        override val icon: SuggestionIcon? = null,
    ) : Suggestion
}
