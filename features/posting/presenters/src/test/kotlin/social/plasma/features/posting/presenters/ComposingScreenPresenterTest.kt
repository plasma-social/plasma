package social.plasma.features.posting.presenters

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.nostrino.crypto.PubKey
import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import org.junit.Test
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.GetUserSuggestions
import social.plasma.domain.interactors.SendNote
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.posting.screens.ComposePostUiEvent.OnBackClick
import social.plasma.features.posting.screens.ComposePostUiEvent.OnNoteChange
import social.plasma.features.posting.screens.ComposePostUiEvent.OnSubmitPost
import social.plasma.features.posting.screens.ComposePostUiEvent.OnUserSuggestionTapped
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.models.TagSuggestion
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeNip5Validator
import social.plasma.shared.repositories.fakes.FakeNoteRepository
import social.plasma.shared.repositories.fakes.FakeUserMetadataRepository
import social.plasma.shared.utils.fakes.FakeStringManager

@OptIn(ExperimentalCoroutinesApi::class)
class ComposingScreenPresenterTest {
    private val navigator = FakeNavigator()
    private val stringManager = FakeStringManager(
        R.string.post to "post",
        R.string.new_note to "new_note",
        R.string.your_message to "your_message",
    )
    private val noteRepository = FakeNoteRepository()
    private val userMetadataRepository = FakeUserMetadataRepository()

    private val TestScope.presenter: ComposingScreenPresenter
        get() {
            return ComposingScreenPresenter(
                navigator = navigator,
                stringManager = stringManager,
                sendNote = SendNote(
                    ioDispatcher = coroutineContext,
                    noteRepository = noteRepository
                ),
                args = ComposingScreen(),
                noteRepository = noteRepository,
                getUserSuggestions = GetUserSuggestions(userMetadataRepository),
                getNip5Status = GetNip5Status(FakeNip5Validator(), coroutineContext),
                accountStateRepository = FakeAccountStateRepository(publicKey = "test".toByteArray()),
                observeMyMetadata = ObserveUserMetadata(userMetadataRepository),
                getHashtagSuggestions = GetHashtagSuggestions(social.plasma.data.daos.fakes.FakeHashTagDao()),
            )
        }

    @Test
    fun `emits default state`() = runTest {
        userMetadataRepository.observeUserMetaDataResult.value = createUserMetadata(
            picture = "avatar-url"
        )
        presenter.test {
            awaitItem()

            with(awaitItem()) {
                assertThat(title).isEqualTo(stringManager[R.string.new_note])
                assertThat(postButtonEnabled).isFalse()
                assertThat(postButtonLabel).isEqualTo(stringManager[R.string.post])
                assertThat(placeholder).isEqualTo(stringManager[R.string.your_message])
                assertThat(showAutoComplete).isFalse()
                assertThat(autoCompleteSuggestions).isEmpty()
                assertThat(avatarUrl).isEqualTo("avatar-url")
            }

        }
    }

    @Test
    fun `emits button enabled when there's input text`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("test")))

            awaitItem()

            assertThat(awaitItem().postButtonEnabled).isTrue()
        }
    }

    @Test
    fun `emits button disabled when input text is cleared`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("test")))
            awaitItem().onEvent(OnNoteChange(TextFieldValue("")))

            awaitItem()
            awaitItem()

            assertThat(awaitItem().postButtonEnabled).isFalse()
        }
    }

    @Test
    fun `navigates back on back click event`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnBackClick)

            assertThat(navigator.awaitPop()).isNotNull()
        }
    }

    @Test
    fun `submits note on submit post event`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("Test content")))

            awaitItem().onEvent(OnSubmitPost)

            awaitItem()
            awaitItem()

            assertThat(awaitItem().postButtonEnabled).isFalse()
        }
    }

    @Test
    fun `when there are suggestions, show suggestions is true`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(
            createUserMetadata(),
        )

        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("@j", selection = TextRange(2))))

            awaitItem()
            awaitItem()

            with(awaitItem()) {
                assertThat(showAutoComplete).isTrue()
                assertThat(autoCompleteSuggestions).isNotEmpty()
            }
        }
    }

    @Test
    fun `when there are no suggestions, show suggestions is false`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(
            createUserMetadata(),
        )

        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("gh")))

            awaitItem()

            with(awaitItem()) {
                assertThat(showAutoComplete).isFalse()
                assertThat(autoCompleteSuggestions).isEmpty()
            }
        }
    }

    @Test
    fun `on suggestion tapped, last word is replaced with npub`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(
            createUserMetadata(),
        )

        presenter.test {
            awaitItem().onEvent(OnNoteChange(TextFieldValue("@j", selection = TextRange(2))))

            awaitItem().onEvent(
                OnUserSuggestionTapped(
                    TagSuggestion(
                        pubKey = pubKey,
                        title = "",
                        nip5Identifier = "",
                        imageUrl = null,
                    )
                )
            )

            awaitItem()
            awaitItem()
            awaitItem()
            awaitItem()

            with(awaitItem()) {
                assertThat(noteContent.text).isEqualTo("@${pubKey.encoded()} ")
            }

        }
    }

    private fun createUserMetadata(
        picture: String? = null,
    ) = UserMetadataEntity(
        name = "test",
        displayName = "",
        about = null,
        createdAt = null,
        banner = null,
        pubkey = "",
        website = null,
        lud = null,
        nip05 = null,
        picture = picture,
    )

    companion object {
        val pubKey =
            PubKey("9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab".decodeHex())
    }
}



