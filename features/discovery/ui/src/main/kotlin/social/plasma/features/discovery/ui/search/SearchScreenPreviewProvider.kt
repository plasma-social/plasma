package social.plasma.features.discovery.ui.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem.SuggestionIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchSuggestionGroup
import social.plasma.features.discovery.screens.search.SearchUiState
import social.plasma.features.discovery.screens.search.UserSearchItem
import social.plasma.models.Nip5Status

internal class SearchScreenPreviewProvider : PreviewParameterProvider<SearchUiState> {
    private val searchSuggestionItems = listOf(
        UserSearchItem(
            pubKeyHex = "test",
            title = "John",
            imageUrl = null,
            nip5Status = Nip5Status.Missing,
        ),
        HashTagSearchSuggestionItem(
            icon = SuggestionIcon.Recent,
            content = "#foodstr",
        ),
        HashTagSearchSuggestionItem(
            icon = SuggestionIcon.Recent,
            content = "#coffeechain",
        )
    )

    private val searchSuggestionGroups = listOf(
        SearchSuggestionGroup(
            title = "RECENT",
            items = searchSuggestionItems,
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
                searchSuggestionGroups = searchSuggestionGroups,
                leadingIcon = SearchBarUiState.LeadingIcon.Back,
                trailingIcon = null,
            ), onEvent = {}
        ),
        SearchUiState(
            searchBarUiState = SearchBarUiState(
                query = "",
                isActive = false,
                suggestionsTitle = null,
                searchSuggestionGroups = emptyList(),
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
                searchSuggestionGroups = searchSuggestionGroups,
                leadingIcon = SearchBarUiState.LeadingIcon.Back,
                trailingIcon = SearchBarUiState.TrailingIcon.Clear,
            ),
            onEvent = {},
        ),
    )
}
