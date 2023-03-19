package social.plasma.onboarding.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginUiEvent
import social.plasma.features.onboarding.screens.login.LoginUiEvent.OnInputChange
import social.plasma.features.onboarding.screens.login.LoginUiEvent.OnLogin
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository


@OptIn(ExperimentalCoroutinesApi::class)
class LoginPresenterTest {
    private val navigator = FakeNavigator()
    private val accountStateRepository = FakeAccountStateRepository()
    private val presenter: LoginPresenter
        get() = LoginPresenter(
            navigator = navigator,
            accountStateRepository = accountStateRepository
        )

    @Test
    fun `initial state`() = runTest {
        presenter.test {
            with(awaitItem()) {
                assertThat(keyInput).isEmpty()
                assertThat(loginButtonVisible).isFalse()
                assertThat(clearInputButtonVisible).isFalse()
            }
        }
    }

    @Test
    fun `when text input changes, clear button is visible`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange("test"))

            with(awaitItem()) {
                assertThat(clearInputButtonVisible).isTrue()
                assertThat(keyInput).isEqualTo("test")
                assertThat(loginButtonVisible).isFalse()
            }
        }
    }

    @Test
    fun `when valid pubkey is entered, login button is visible`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange(npub))
            awaitItem()
            with(awaitItem()) {
                assertThat(loginButtonVisible).isTrue()
                assertThat(keyInput).isEqualTo(npub)
                assertThat(clearInputButtonVisible).isTrue()
            }
        }
    }

    @Test
    fun `when valid seckey is entered, login button is visible`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange(nsec))
            awaitItem()

            with(awaitItem()) {
                assertThat(loginButtonVisible).isTrue()
                assertThat(keyInput).isEqualTo(nsec)
                assertThat(clearInputButtonVisible).isTrue()
            }
        }
    }

    @Test
    fun `on input clear event, the input is cleared`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange(npub))
            awaitItem().onEvent(LoginUiEvent.OnClearInput)

            awaitItem()
            awaitItem()

            with(awaitItem()) {
                assertThat(loginButtonVisible).isFalse()
                assertThat(keyInput).isEmpty()
                assertThat(clearInputButtonVisible).isFalse()
            }
        }
    }

    @Test
    fun `when login with npub, store the key and navigate home`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange(npub))
            awaitItem()

            awaitItem().onEvent(OnLogin)

            assertThat(navigator.awaitNextScreen()).isEqualTo(HomeScreen)
            assertThat(accountStateRepository.getPublicKey()).isNotNull()
        }
    }

    @Test
    fun `when login with nsec, store the key and navigate home`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnInputChange(nsec))
            awaitItem()

            awaitItem().onEvent(OnLogin)

            assertThat(navigator.awaitNextScreen()).isEqualTo(HomeScreen)
            assertThat(accountStateRepository.getSecretKey()).isNotNull()
        }
    }

    companion object {
        const val npub = "npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"
        const val nsec = "nsec16pzcl7krecytvxdj28wlzver8tuwfvfs6wytjn7plyyvp6acdkzq705x7t"
    }
}