package social.plasma.features.search.screens

import com.slack.circuit.CircuitUiState

data class SearchUiState(
    val onEvent: (SearchUiEvent) -> Unit,
    val searchBarUiState: SearchBarUiState = SearchBarUiState(
        query = "",
        isActive = false,
        suggestionsTitle = null,
        suggestions = emptyList()
    ),
) : CircuitUiState

data class SearchBarUiState(
    val query: String,
    val isActive: Boolean,
    val suggestionsTitle: String?,
    val suggestions: List<Suggestion>,
)

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
