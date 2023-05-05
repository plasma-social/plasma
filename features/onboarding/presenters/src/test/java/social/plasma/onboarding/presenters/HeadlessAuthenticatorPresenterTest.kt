package social.plasma.onboarding.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import social.plasma.domain.interactors.GetAuthStatus
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginScreen
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import java.util.UUID


@OptIn(ExperimentalCoroutinesApi::class)
class HeadlessAuthenticatorPresenterTest {
    private val accountStateRepository: AccountStateRepository = FakeAccountStateRepository()
    private val navigator = FakeNavigator()
    private val getAuthStatus = GetAuthStatus(accountStateRepository)

    private val presenter: HeadlessAuthenticatorPresenter
        get() = presenter(HeadlessAuthenticator())

    private fun presenter(args: HeadlessAuthenticator): HeadlessAuthenticatorPresenter {
        return HeadlessAuthenticatorPresenter(
            navigator = navigator,
            getAuthStatus = getAuthStatus,
            args = args
        )
    }

    @Before
    fun setup() {
        accountStateRepository.clearKeys()
    }

    @After
    fun tearDown() {
        navigator.assertIsEmpty()
    }

    @Test
    fun `if user authenticated go home`() = runTest {
        accountStateRepository.setSecretKey(UUID.randomUUID().toString().encodeToByteArray())

        presenter.test {
            awaitItem()

            assertThat(navigator.awaitResetRoot()).isEqualTo(HomeScreen)
        }
    }

    @Test
    fun `if user logged in with pubkey go home`() = runTest {
        accountStateRepository.setPublicKey(UUID.randomUUID().toString().encodeToByteArray())

        presenter.test {
            awaitItem()

            assertThat(navigator.awaitResetRoot()).isEqualTo(HomeScreen)
        }
    }

    @Test
    fun `if user not authenticated go to login`() = runTest {
        presenter.test {
            awaitItem()

            assertThat(navigator.awaitResetRoot()).isEqualTo(LoginScreen)
        }
    }

    @Test
    fun `if args has exit screen go to exit screen`() = runTest {
        accountStateRepository.setPublicKey(UUID.randomUUID().toString().encodeToByteArray())
        val args = HeadlessAuthenticator(exitScreen = LoginScreen)
        presenter(args).test {
            awaitItem()

            assertThat(navigator.awaitResetRoot()).isEqualTo(HomeScreen)
            assertThat(navigator.awaitNextScreen()).isEqualTo(LoginScreen)
        }
    }
}
