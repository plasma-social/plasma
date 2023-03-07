package social.plasma.ui.login

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import extensions.MainDispatcherExtension
import fakes.FakeByteArrayPreference
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import social.plasma.prefs.Preference
import social.plasma.repository.RealAccountRepository

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({
    extensions(MainDispatcherExtension)

    "entering a valid public key" {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.filterIsInstance<LoginState.LoggedOut>().test {
                assertInitialItem()

                viewModel.onKeyChanged(PUBLIC_KEY)

                awaitItem() shouldBe LoginState.LoggedOut(
                    keyInput = PUBLIC_KEY,
                    loginButtonVisible = false,
                    clearInputButtonVisible = true
                )

                awaitItem() shouldBe LoginState.LoggedOut(
                    keyInput = PUBLIC_KEY,
                    loginButtonVisible = true,
                    clearInputButtonVisible = true
                )

                ensureAllEventsConsumed()
            }
        }
    }

    "entering a valid secret key" {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.filterIsInstance<LoginState.LoggedOut>().test {
                assertInitialItem()

                viewModel.onKeyChanged(SECRET_KEY)

                awaitItem() shouldBe LoginState.LoggedOut(
                    keyInput = SECRET_KEY,
                    loginButtonVisible = false,
                    clearInputButtonVisible = true
                )

                awaitItem() shouldBe LoginState.LoggedOut(
                    keyInput = SECRET_KEY,
                    loginButtonVisible = true,
                    clearInputButtonVisible = true
                )

                ensureAllEventsConsumed()
            }
        }
    }

    "entering an invalid key" {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.filterIsInstance<LoginState.LoggedOut>().test {
                assertInitialItem()

                viewModel.onKeyChanged(INVALID_KEY)

                awaitItem() shouldBe LoginState.LoggedOut(
                    keyInput = INVALID_KEY,
                    loginButtonVisible = false,
                    clearInputButtonVisible = true
                )

                ensureAllEventsConsumed()
            }
        }
    }

    // TODO fake bech32 utils
//    "tapping login button stores key" {
//        runTest {
//            val userKeyPref = FakeByteArrayPreference()
//            val viewModel = createViewModel(userKeyPref)
//
//            viewModel.uiState.filterIsInstance<LoginState.LoggedIn>().test {
//                viewModel.onKeyChanged(SECRET_KEY)
//
//                viewModel.login()
//
//                awaitItem()
//                userKeyPref.get(null) shouldBe SECRET_KEY
//
//                ensureAllEventsConsumed()
//            }
//        }
//    }

    "shows clear input button only when there's input" {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.filterIsInstance<LoginState.LoggedOut>().test {
                assertInitialItem()

                viewModel.onKeyChanged("   ")

                awaitItem().clearInputButtonVisible shouldBe true

                viewModel.onKeyChanged("")

                awaitItem().clearInputButtonVisible shouldBe false

                ensureAllEventsConsumed()
            }
        }
    }
}) {
    companion object {
        const val SECRET_KEY = "nsec16pzcl7krecytvxdj28wlzver8tuwfvfs6wytjn7plyyvp6acdkzq705x7t"
        const val PUBLIC_KEY = "npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"
        const val INVALID_KEY = "npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun createViewModel(
    secretKeyPref: Preference<ByteArray> = FakeByteArrayPreference(),
    pubKeyPref: Preference<ByteArray> = FakeByteArrayPreference(),
) = LoginViewModel(
    accountStateRepo = RealAccountRepository(
        secretKey = secretKeyPref,
        publicKey = pubKeyPref,
    ),
    defaultDispatcher = StandardTestDispatcher()
)

private suspend fun ReceiveTurbine<LoginState.LoggedOut>.assertInitialItem() {
    awaitItem() shouldBe LoginState.LoggedOut(
        keyInput = "",
        loginButtonVisible = false,
        clearInputButtonVisible = false
    )
}
