package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetPopularHashTags
import social.plasma.domain.interactors.GetUserSuggestions
import social.plasma.domain.observers.ObserveCurrentUserMetadata
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
import social.plasma.models.HashTag

class SearchScreenPresenter @AssistedInject constructor(
    private val getPopularHashTags: GetPopularHashTags,
    private val getUserSuggestions: GetUserSuggestions,
    private val getHashtagSuggestions: GetHashtagSuggestions,
    private val observeCurrentUserMetadata: ObserveCurrentUserMetadata,
    @Assisted private val forceActive: Boolean,
    @Assisted private val navigator: Navigator,
) : Presenter<SearchUiState> {

    private val userMetadataFlow = observeCurrentUserMetadata.flow.onStart {
        observeCurrentUserMetadata(Unit)
    }

    @Composable
    override fun present(): SearchUiState {
        var query by rememberSaveable { mutableStateOf("") }
        val userMetadata by rememberRetained { userMetadataFlow }.collectAsState(null)
        val userSuggestions by remember { getUserSuggestions.flow }.collectAsState(emptyList())
        LaunchedEffect(query) {
            if (query.startsWith("#").not()) {
                getUserSuggestions(GetUserSuggestions.Params(query))
            }
        }

        // TODO - uncomment once we add recommendations back.
//        var isActive by rememberSaveable { mutableStateOf(false) }

//        val leadingIcon =
//            remember(isActive) { if (isActive) LeadingIcon.Back else LeadingIcon.Search }

        val (activeState, setActive) = rememberSaveable { mutableStateOf(false) }

        val isActive = if (forceActive) remember { true } else {
            activeState
        }

        val leadingIcon =
            remember(isActive) { if (isActive && !forceActive) LeadingIcon.Back else LeadingIcon.Search }

        val trailingIcon = remember(query, isActive, userMetadata) {
            when (isActive) {
                true -> if (query.isEmpty()) null else TrailingIcon.Clear
                false -> TrailingIcon.Avatar(userMetadata?.picture)
            }
        }

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

        val suggestedUserItems by produceState<List<SearchSuggestion>>(
            initialValue = emptyList(),
            userSuggestions
        ) {
            value = userSuggestions.map {
                UserSearchItem(
                    pubKeyHex = it.pubKey.hex(),
                    imageUrl = it.imageUrl,
                    title = it.title,
                    nip5Status = it.nip5Status,
                )
            }
        }

        val searchResultItems by produceState<List<SearchSuggestionGroup>>(
            initialValue = emptyList(),
            popularHashTags,
            suggestedHashTags,
            suggestedUserItems,
        ) {
            val suggestions = mutableListOf<SearchSuggestionGroup>().apply {
                if (suggestedUserItems.isNotEmpty()) {
                    add(SearchSuggestionGroup(title = "Users", suggestedUserItems))
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
                is SearchUiEvent.OnActiveChanged -> setActive(event.active)
                is SearchUiEvent.OnQueryChanged -> query = event.query
                SearchUiEvent.OnSearch -> setActive(false)
                SearchUiEvent.OnLeadingIconTapped -> setActive(
                    when (leadingIcon) {
                        LeadingIcon.Back -> false
                        LeadingIcon.Search -> true
                    }
                )

                SearchUiEvent.OnTrailingIconTapped -> when (trailingIcon) {
                    TrailingIcon.Clear -> query = ""
                    is TrailingIcon.Avatar -> userMetadata?.let {
                        navigator.goTo(
                            ProfileScreen(
                                it.pubkey
                            )
                        )
                    }

                    null -> {}
                }

                is SearchUiEvent.OnSearchSuggestionTapped -> when (val item = event.item) {
                    is HashTagSearchSuggestionItem -> navigator.goTo(
                        HashTagFeedScreen(HashTag.parse(item.content))
                    )

                    is UserSearchItem -> navigator.goTo(ProfileScreen(item.pubKeyHex))
                }
            }
        })
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, forceActive: Boolean): SearchScreenPresenter
    }
}
