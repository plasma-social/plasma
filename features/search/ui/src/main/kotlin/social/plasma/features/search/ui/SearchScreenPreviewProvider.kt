package social.plasma.features.search.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import social.plasma.features.search.screens.SearchBarUiState
import social.plasma.features.search.screens.SearchUiState
import social.plasma.features.search.screens.Suggestion

internal class SearchScreenPreviewProvider : PreviewParameterProvider<SearchUiState> {
    private val recentSearchesSuggestionsState = SearchUiState(
        onEvent = {}, searchBarUiState = SearchBarUiState(
            query = "",
            isActive = true,
            suggestionsTitle = "RECENT",
            suggestions = listOf(
                Suggestion.UserSuggestion(
                    icon = Suggestion.SuggestionIcon.Recent,
                    content = "John",
                ),
                Suggestion.CommunitySuggestion(
                    icon = Suggestion.SuggestionIcon.Recent,
                    content = "#foodstr",
                ),
                Suggestion.CommunitySuggestion(
                    icon = Suggestion.SuggestionIcon.Recent,
                    content = "#coffeechain",
                )
            )
        )
    )

    private val defaultState = SearchUiState(
        onEvent = {}, searchBarUiState = SearchBarUiState(
            query = "",
            isActive = false,
            suggestionsTitle = null,
            suggestions = emptyList()
        )
    )

    override val values: Sequence<SearchUiState> = sequenceOf(
        recentSearchesSuggestionsState,
        defaultState,
    )
}
