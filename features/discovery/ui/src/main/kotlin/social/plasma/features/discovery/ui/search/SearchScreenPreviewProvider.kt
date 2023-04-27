package social.plasma.features.discovery.ui.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchUiState
import social.plasma.features.discovery.screens.search.Suggestion

internal class SearchScreenPreviewProvider : PreviewParameterProvider<SearchUiState> {
    private val suggestions = listOf(
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

    /**
     * These states also used for snapshot tests.
     * Paparazzi uses the order of the values to generate the names of the snapshots,
     * so make sure to add new states to the end of the sequence.
     *
     * To add new previews that are not used for snapshot tests, add them to the [SearchScreenUi] file directly.
     */
    override val values: Sequence<SearchUiState> = sequenceOf(
        SearchUiState(
            searchBarUiState = SearchBarUiState(
                query = "",
                isActive = true,
                suggestionsTitle = "RECENT",
                suggestions = suggestions,
                leadingIcon = SearchBarUiState.LeadingIcon.Back,
                trailingIcon = null,
            ), onEvent = {}
        ),
        SearchUiState(
            searchBarUiState = SearchBarUiState(
                query = "",
                isActive = false,
                suggestionsTitle = null,
                suggestions = emptyList(),
                leadingIcon = SearchBarUiState.LeadingIcon.Search,
                trailingIcon = null,
            ),
            onEvent = {}
        ),
        SearchUiState(
            searchBarUiState = SearchBarUiState(
                query = "test",
                isActive = true,
                suggestionsTitle = "RECENT",
                suggestions = suggestions,
                leadingIcon = SearchBarUiState.LeadingIcon.Back,
                trailingIcon = SearchBarUiState.TrailingIcon.Clear,
            ),
            onEvent = {},
        ),
    )
}
