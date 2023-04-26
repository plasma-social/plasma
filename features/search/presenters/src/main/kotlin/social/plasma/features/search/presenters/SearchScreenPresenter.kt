package social.plasma.features.search.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.search.screens.SearchBarUiState
import social.plasma.features.search.screens.SearchBarUiState.LeadingIcon
import social.plasma.features.search.screens.SearchBarUiState.TrailingIcon
import social.plasma.features.search.screens.SearchUiEvent
import social.plasma.features.search.screens.SearchUiState
import social.plasma.features.search.screens.Suggestion

class SearchScreenPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<SearchUiState> {

    private val fakeRecentSuggestions = listOf(
        Suggestion.UserSuggestion(
            content = "John",
            icon = Suggestion.SuggestionIcon.Recent,
        ),
        Suggestion.CommunitySuggestion(
            content = "#foodstr",
            icon = Suggestion.SuggestionIcon.Recent,
        ),
        Suggestion.UserSuggestion(
            content = "Jane",
            icon = Suggestion.SuggestionIcon.Recent,
        ),
        Suggestion.CommunitySuggestion(
            content = "#coffeechain",
            icon = Suggestion.SuggestionIcon.Recent,
        ),
    )

    @Composable
    override fun present(): SearchUiState {
        var query by rememberSaveable { mutableStateOf("") }
        var isActive by rememberSaveable { mutableStateOf(false) }

        val leadingIcon =
            remember(isActive) { if (isActive) LeadingIcon.Back else LeadingIcon.Search }

        val trailingIcon =
            remember(query) { if (query.isEmpty()) null else TrailingIcon.Clear }

        return SearchUiState(searchBarUiState = SearchBarUiState(
            query = query,
            isActive = isActive,
            suggestionsTitle = if (isActive) "RECENT" else null,
            suggestions = if (isActive) fakeRecentSuggestions else emptyList(),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        ), onEvent = { event ->
            when (event) {
                is SearchUiEvent.OnActiveChanged -> isActive = event.active
                is SearchUiEvent.OnQueryChanged -> query = event.query
                SearchUiEvent.OnSearch -> isActive = false
                SearchUiEvent.OnLeadingIconTapped -> isActive = when (leadingIcon) {
                    LeadingIcon.Back -> false
                    LeadingIcon.Search -> true
                }

                SearchUiEvent.OnTrailingIconTapped -> if (trailingIcon == TrailingIcon.Clear) query =
                    ""
            }
        })
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): SearchScreenPresenter
    }
}
