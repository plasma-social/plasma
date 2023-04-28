package social.plasma.discovery.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.data.daos.fakes.FakeHashTagDao
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetPopularHashTags
import social.plasma.features.discovery.presenters.SearchScreenPresenter
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchBarUiState.LeadingIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState.TrailingIcon
import social.plasma.features.discovery.screens.search.SearchSuggestion.HashTagSearchSuggestionItem
import social.plasma.features.discovery.screens.search.SearchSuggestion.SuggestionIcon
import social.plasma.features.discovery.screens.search.SearchSuggestionGroup
import social.plasma.features.discovery.screens.search.SearchUiEvent
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnActiveChanged
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnLeadingIconTapped
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnQueryChanged
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnSearchSuggestionTapped
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen


@OptIn(ExperimentalCoroutinesApi::class)
class SearchScreenPresenterTest {
    private val navigator = FakeNavigator()
    private val hashtagsDao = FakeHashTagDao()
    private val getPopularHashTags = GetPopularHashTags(hashtagsDao)
    private val getHashTagSuggestions = GetHashtagSuggestions(hashtagsDao)


    private val presenter: SearchScreenPresenter
        get() = SearchScreenPresenter(
            getPopularHashTags = getPopularHashTags,
            getHashtagSuggestions = getHashTagSuggestions,
            navigator = navigator,
        )

    @Test
    fun `initial state`() = runTest {
        presenter.test {
            with(awaitItem()) {
                assertThat(this.searchBarUiState).isEqualTo(
                    SearchBarUiState(
                        query = "",
                        isActive = false,
                        suggestionsTitle = null,
                        searchSuggestionGroups = emptyList(),
                        leadingIcon = LeadingIcon.Search,
                        trailingIcon = null,
                    )
                )
            }
        }
    }

    @Test
    fun `activating searchbar shows back arrow`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnActiveChanged(true))

            assertThat(awaitItem().searchBarUiState.leadingIcon).isEqualTo(
                LeadingIcon.Back,
            )
        }
    }

    @Test
    fun `query updates clear icon`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnQueryChanged("test"))

            with(awaitItem()) {
                assertThat(searchBarUiState.trailingIcon).isEqualTo(TrailingIcon.Clear)

                onEvent(OnQueryChanged(""))

                assertThat(awaitItem().searchBarUiState.trailingIcon).isNull()
            }
        }
    }

    @Test
    fun `on back arrow tapped, searchbar is deactivated`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnActiveChanged(true))

            with(awaitItem()) {
                assertThat(searchBarUiState.isActive).isTrue()
                assertThat(searchBarUiState.leadingIcon).isEqualTo(LeadingIcon.Back)

                onEvent(OnLeadingIconTapped)

                assertThat(awaitItem().searchBarUiState.isActive).isFalse()
            }
        }
    }

    @Test
    fun `on search icon tapped, searchbar is activated`() = runTest {
        presenter.test {
            with(awaitItem()) {
                assertThat(searchBarUiState.isActive).isFalse()
                assertThat(searchBarUiState.leadingIcon).isEqualTo(LeadingIcon.Search)

                onEvent(OnLeadingIconTapped)

                assertThat(awaitItem().searchBarUiState.isActive).isTrue()
            }
        }
    }

    @Test
    fun `on clear icon tapped, query is cleared`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnQueryChanged("test"))

            with(awaitItem()) {
                assertThat(searchBarUiState.query).isEqualTo("test")
                assertThat(searchBarUiState.trailingIcon).isEqualTo(TrailingIcon.Clear)

                onEvent(SearchUiEvent.OnTrailingIconTapped)

                assertThat(awaitItem().searchBarUiState.query).isEmpty()
            }
        }
    }

    @Test
    fun `search results contain popular hashtags`() = runTest {
        hashtagsDao.popularHashTags.value = (1..5).map { "$it" }

        val expectedHashTags = (1..5).map {
            HashTagSearchSuggestionItem(
                content = "#$it",
                icon = SuggestionIcon.Popular
            )
        }

        val expectedSearchSuggestionGroups = listOf(
            SearchSuggestionGroup(
                title = "Popular",
                items = expectedHashTags
            )
        )

        presenter.test {
            awaitItem().onEvent(OnActiveChanged(true))

            awaitItem()

            with(awaitItem()) {
                assertThat(searchBarUiState.searchSuggestionGroups).isEqualTo(
                    expectedSearchSuggestionGroups
                )
            }
        }
    }

    @Test
    fun `search results contain recommended hashtags`() = runTest {
        hashtagsDao.popularHashTags.value = (1..5).map { "$it" }
        hashtagsDao.hashTagRecommendations.value = (1..5).map { "$it" }

        val expectedSearchSuggestionGroups = listOf(
            SearchSuggestionGroup(
                title = null,
                items = (1..5).map {
                    HashTagSearchSuggestionItem(
                        content = "#$it",
                        icon = null,
                    )
                }
            ),
            SearchSuggestionGroup(
                title = "Popular",
                items = (1..5).map {
                    HashTagSearchSuggestionItem(
                        content = "#$it",
                        icon = SuggestionIcon.Popular
                    )
                }
            ),
        )

        presenter.test {
            awaitItem().onEvent(OnActiveChanged(true))
            awaitItem().onEvent(OnQueryChanged("test"))

            repeat(3) {
                awaitItem()
            }

            with(awaitItem()) {
                assertThat(searchBarUiState.searchSuggestionGroups).isEqualTo(
                    expectedSearchSuggestionGroups
                )
            }
        }
    }

    @Test
    fun `tapping on hashtag suggestion opens community feed`() = runTest {
        presenter.test {
            awaitItem().onEvent(
                OnSearchSuggestionTapped(
                    HashTagSearchSuggestionItem(
                        "test",
                        SuggestionIcon.Popular
                    )
                )
            )

            assertThat(navigator.awaitNextScreen()).isEqualTo(HashTagFeedScreen("test"))
        }
    }
}
