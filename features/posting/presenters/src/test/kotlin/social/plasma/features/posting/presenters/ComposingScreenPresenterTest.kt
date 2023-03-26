package social.plasma.features.posting.presenters

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.domain.interactors.SendNote
import social.plasma.features.posting.screens.ComposePostUiEvent.OnBackClick
import social.plasma.features.posting.screens.ComposePostUiEvent.OnNoteChange
import social.plasma.features.posting.screens.ComposePostUiEvent.OnSubmitPost
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.shared.repositories.fakes.FakeNoteRepository
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
                noteRepository = noteRepository
            )
        }

    @Test
    fun `emits default state`() = runTest {
        presenter.test {
            with(awaitItem()) {
                assertThat(title).isEqualTo(stringManager[R.string.new_note])
                assertThat(postButtonEnabled).isFalse()
                assertThat(postButtonLabel).isEqualTo(stringManager[R.string.post])
                assertThat(placeholder).isEqualTo(stringManager[R.string.your_message])
            }
        }
    }

    @Test
    fun `emits button enabled when there's input text`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnNoteChange("test"))

            awaitItem()

            assertThat(awaitItem().postButtonEnabled).isTrue()
        }
    }

    @Test
    fun `emits button disabled when input text is cleared`() = runTest {
        presenter.test {
            awaitItem().onEvent(OnNoteChange("test"))
            awaitItem().onEvent(OnNoteChange(""))

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
            awaitItem().onEvent(OnNoteChange("Test content"))

            awaitItem().onEvent(OnSubmitPost)

            awaitItem()
            awaitItem()

            assertThat(awaitItem().postButtonEnabled).isFalse()
        }
    }
}



