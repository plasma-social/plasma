package social.plasma.search.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.features.search.presenters.SearchScreenPresenter
import social.plasma.features.search.screens.SearchUiState


@OptIn(ExperimentalCoroutinesApi::class)
class SearchScreenPresenterTest {
    private val navigator = FakeNavigator()
    private val presenter: SearchScreenPresenter
        get() = SearchScreenPresenter(
            navigator = navigator,
        )

    @Test
    fun `initial state`() = runTest {
        presenter.test {
            assertThat(awaitItem()).isInstanceOf(SearchUiState::class.java)
        }
    }
}
