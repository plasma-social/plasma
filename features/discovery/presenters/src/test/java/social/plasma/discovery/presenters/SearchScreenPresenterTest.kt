package social.plasma.discovery.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.data.daos.fakes.FakeHashTagDao
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.GetPopularHashTags
import social.plasma.domain.interactors.GetUserSuggestions
import social.plasma.domain.observers.ObserveCurrentUserMetadata
import social.plasma.features.discovery.presenters.SearchScreenPresenter
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem.SuggestionIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchBarUiState.LeadingIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState.TrailingIcon
import social.plasma.features.discovery.screens.search.SearchSuggestionGroup
import social.plasma.features.discovery.screens.search.SearchUiEvent
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnActiveChanged
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnLeadingIconTapped
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnQueryChanged
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnSearchSuggestionTapped
import social.plasma.features.discovery.screens.search.UserSearchItem
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.models.HashTag
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeNip5Validator
import social.plasma.shared.repositories.fakes.FakeUserMetadataRepository
import kotlin.coroutines.EmptyCoroutineContext


class SearchScreenPresenterTest {
    private val navigator = FakeNavigator()
    private val hashtagsDao = FakeHashTagDao()
    private val getPopularHashTags = GetPopularHashTags(hashtagsDao)
    private val getHashTagSuggestions = GetHashtagSuggestions(hashtagsDao)
    private val userMetadataRepository = FakeUserMetadataRepository()

    private val getUserSuggestions = GetUserSuggestions(
        userMetadataRepository = userMetadataRepository,
        getNip5Status = GetNip5Status(
            FakeNip5Validator(), EmptyCoroutineContext
        ),
        coroutineContext = EmptyCoroutineContext,
    )
    private val presenter: SearchScreenPresenter
        get() = SearchScreenPresenter(
            getPopularHashTags = getPopularHashTags,
            getHashtagSuggestions = getHashTagSuggestions,
            getUserSuggestions = getUserSuggestions,
            observeCurrentUserMetadata = ObserveCurrentUserMetadata(
                userMetadataRepository.apply {
                    observeUserMetaDataResult.value = USER_METADATA
                },
                FakeAccountStateRepository().apply {
                    setPublicKey(USER_METADATA.pubkey.toByteArray())
                }
            ),
            navigator = navigator,
            forceActive = false,
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
                        trailingIcon = TrailingIcon.Avatar(null),
                    )
                )
            }

            with(awaitItem()) {
                assertThat(this.searchBarUiState).isEqualTo(
                    SearchBarUiState(
                        query = "",
                        isActive = false,
                        suggestionsTitle = null,
                        searchSuggestionGroups = emptyList(),
                        leadingIcon = LeadingIcon.Search,
                        trailingIcon = TrailingIcon.Avatar(USER_METADATA.picture),
                    )
                )
            }
        }
    }

    @Test
    fun `activating searchbar shows back arrow`() = runTest {
        presenter.test {
            awaitItem()

            awaitItem().onEvent(OnActiveChanged(true))

            assertThat(awaitItem().searchBarUiState.leadingIcon).isEqualTo(
                LeadingIcon.Back,
            )
        }
    }

    @Test
    fun `query updates clear icon`() = runTest {
        presenter.test {

            awaitItem().onEvent(OnActiveChanged(true))
            awaitItem().onEvent(OnQueryChanged("test"))

            awaitItem()

            with(awaitItem()) {
                assertThat(searchBarUiState.trailingIcon).isEqualTo(TrailingIcon.Clear)

                onEvent(OnQueryChanged(""))

                assertThat(awaitItem().searchBarUiState.trailingIcon).isEqualTo(null)
            }
        }
    }

    @Test
    fun `on back arrow tapped, searchbar is deactivated`() = runTest {
        presenter.test {
            awaitItem()

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
            awaitItem()

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
            awaitItem()

            awaitItem().onEvent(OnActiveChanged(true))
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
            awaitItem()

            awaitItem().onEvent(
                OnSearchSuggestionTapped(
                    HashTagSearchSuggestionItem(
                        "test",
                        SuggestionIcon.Popular
                    )
                )
            )

            assertThat(navigator.awaitNextScreen()).isEqualTo(HashTagFeedScreen(HashTag.parse("test")))
        }
    }

    @Test
    fun `tapping on an user suggestion opens profile screen`() = runTest {
        presenter.test {
            awaitItem()

            awaitItem().onEvent(
                OnSearchSuggestionTapped(
                    UserSearchItem(
                        pubKeyHex = "test",
                        imageUrl = null,
                        title = "test",
                        nip5Identifier = null,
                        isNip5Valid = null,
                    )
                )
            )

            assertThat(navigator.awaitNextScreen()).isEqualTo(ProfileScreen(pubKeyHex = "test"))
        }
    }

    @Test
    fun `if query starts with # sign, search results contain only hashtags`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnActiveChanged(true))
            awaitItem().onEvent(OnQueryChanged("#test"))

            repeat(4) {
                awaitItem()
            }

            userMetadataRepository.searchUsersCalls.expectNoEvents()
        }
    }

    companion object {
        private val USER_METADATA = UserMetadataEntity(
            pubkey = "test",
            name = null,
            about = null,
            picture = "testavatar",
            banner = null,
            displayName = null,
            nip05 = null,
            lud06 = null,
            lud16 = null,
            website = null,
            createdAt = null,
        )
    }
}
