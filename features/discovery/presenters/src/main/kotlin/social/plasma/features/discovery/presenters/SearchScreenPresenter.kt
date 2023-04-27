package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetPopularHashTags
import social.plasma.domain.interactors.GetUserSuggestions
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem.SuggestionIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchBarUiState.LeadingIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState.TrailingIcon
import social.plasma.features.discovery.screens.search.SearchSuggestion
import social.plasma.features.discovery.screens.search.SearchSuggestionGroup
import social.plasma.features.discovery.screens.search.SearchUiEvent
import social.plasma.features.discovery.screens.search.SearchUiState
import social.plasma.features.discovery.screens.search.UserSearchItem
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.profile.screens.ProfileScreen

class SearchScreenPresenter @AssistedInject constructor(
    private val getPopularHashTags: GetPopularHashTags,
    private val getUserSuggestions: GetUserSuggestions,
    private val getHashtagSuggestions: GetHashtagSuggestions,
    @Assisted private val navigator: Navigator,
) : Presenter<SearchUiState> {

    @Composable
    override fun present(): SearchUiState {
        var query by rememberSaveable { mutableStateOf("") }

        var isActive by rememberSaveable { mutableStateOf(false) }

        val leadingIcon =
            remember(isActive) { if (isActive) LeadingIcon.Back else LeadingIcon.Search }

        val trailingIcon =
            remember(query) { if (query.isEmpty()) null else TrailingIcon.Clear }

        val popularHashTags by produceState<List<SearchSuggestion>>(initialValue = emptyList()) {
            val suggestions = getPopularHashTags.executeSync(GetPopularHashTags.Params(10)).map {
                HashTagSearchSuggestionItem(
                    content = "#$it",
                    icon = SuggestionIcon.Popular,
                )
            }

            value = suggestions
        }

        val suggestedHashTags by produceState<List<SearchSuggestion>>(
            initialValue = emptyList(),
            query
        ) {
            value = if (query.isEmpty()) emptyList() else getHashtagSuggestions.executeSync(
                GetHashtagSuggestions.Params(query)
            ).map {
                HashTagSearchSuggestionItem(
                    content = "#$it",
                    icon = null,
                )
            }
        }

        val suggestedUsers by produceState<List<SearchSuggestion>>(
            initialValue = emptyList(),
            query
        ) {
            value = if (query.isEmpty()) emptyList() else getUserSuggestions.executeSync(
                GetUserSuggestions.Params(query, query.length)
            ).map {
                UserSearchItem(
                    pubKeyHex = it.pubKey.hex(),
                    imageUrl = it.imageUrl,
                    title = it.title,
                    nip5Identifier = it.nip5Identifier,
                )
            }
        }

        val searchResultItems by produceState<List<SearchSuggestionGroup>>(
            initialValue = emptyList(),
            popularHashTags,
            suggestedHashTags,
            suggestedUsers,
        ) {
            val suggestions = mutableListOf<SearchSuggestionGroup>().apply {
                if (suggestedUsers.isNotEmpty()) {
                    add(SearchSuggestionGroup(title = "Users", suggestedUsers))
                }
                if (suggestedHashTags.isNotEmpty()) {
                    add(
                        SearchSuggestionGroup(
                            title = null,
                            suggestedHashTags
                        )
                    )
                }
                if (popularHashTags.isNotEmpty()) {
                    add(
                        SearchSuggestionGroup(
                            title = "Popular",
                            popularHashTags
                        )
                    )
                }
            }

            value = suggestions
        }

        return SearchUiState(searchBarUiState = SearchBarUiState(
            query = query,
            isActive = isActive,
            suggestionsTitle = if (isActive) "RECENT" else null,
            searchSuggestionGroups = if (isActive) searchResultItems else emptyList(),
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

                is SearchUiEvent.OnSearchSuggestionTapped -> when (val item = event.item) {
                    is HashTagSearchSuggestionItem -> navigator.goTo(HashTagFeedScreen(item.content))
                    is UserSearchItem -> navigator.goTo(ProfileScreen(item.pubKeyHex))
                }
            }
        })
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): SearchScreenPresenter
    }
}
