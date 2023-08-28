package social.plasma.onboarding.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.domain.observers.ObserveHasPendingNotifications
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnFabClick
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeNoteRepository

class HomePresenterTest {
    private val navigator = FakeNavigator()
    private val accountStateRepository = FakeAccountStateRepository()
    private val notesRepo = FakeNoteRepository()

    private val observeHasPendingNotifications =
        ObserveHasPendingNotifications(accountStateRepository, notesRepo)
    private val presenter
        get() = HomePresenter(observeHasPendingNotifications, navigator)

    @Test
    fun `tapping on the fab multiple times`() = runTest {
        presenter.test {
            with(awaitItem()) {
                repeat(2) {
                    onEvent(OnFabClick)
                }
            }


            assertThat(navigator.awaitNextScreen()).isEqualTo(ComposingScreen())
            navigator.assertIsEmpty()
        }
    }
}
