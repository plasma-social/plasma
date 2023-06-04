package social.plasma.feeds.presenters.hashtag

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.data.daos.fakes.FakeEventsDao
import social.plasma.data.daos.fakes.FakeLastRequestDao
import social.plasma.data.nostr.fakes.FakeRelayManager
import social.plasma.domain.interactors.FollowHashTag
import social.plasma.domain.interactors.StoreEvents
import social.plasma.domain.interactors.SyncHashTagEvents
import social.plasma.domain.interactors.UnfollowHashTag
import social.plasma.domain.observers.ObserveFollowedHashTags
import social.plasma.domain.observers.ObservePagedHashTagFeed
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.screens.hashtags.ButtonUiState
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.models.HashTag
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeContactsRepository
import social.plasma.shared.repositories.fakes.FakeNoteRepository
import social.plasma.shared.utils.fakes.FakeStringManager
import kotlin.coroutines.EmptyCoroutineContext

class HashTagScreenPresenterTest {
    private val navigator = FakeNavigator()
    private val stringManager = FakeStringManager(
        R.string.join to "Join",
        R.string.leave to "Leave",
    )
    private val noteRepository = FakeNoteRepository()
    private val contactsRepository = FakeContactsRepository()

    @Test
    fun `hashtag is not followed`() = runTest {
        makePresenter().test {
            with(awaitItem()) {
                assertThat(title).isEqualTo("#test")
                assertThat(followButtonUiState).isEqualTo(
                    ButtonUiState(
                        label = stringManager[R.string.join],
                        style = ButtonUiState.Style.Primary,
                    )
                )
                onEvent(HashTagScreenUiEvent.OnFollowButtonClick)
            }

            with(awaitItem()) {
                assertThat(title).isEqualTo("#test")
                assertThat(followButtonUiState).isEqualTo(
                    ButtonUiState(
                        label = stringManager[R.string.leave],
                        style = ButtonUiState.Style.PrimaryOutline,
                    )
                )
            }
        }
    }

    @Test
    fun `hashtag is followed`() = runTest {
        contactsRepository.followHashTag(HashTag.parse("test"))

        makePresenter().test {
            awaitItem()

            with(awaitItem()) {
                assertThat(title).isEqualTo("#test")
                assertThat(followButtonUiState).isEqualTo(
                    ButtonUiState(
                        label = stringManager[R.string.leave],
                        style = ButtonUiState.Style.PrimaryOutline,
                    )
                )
                onEvent(HashTagScreenUiEvent.OnFollowButtonClick)
            }

            with(awaitItem()) {
                assertThat(title).isEqualTo("#test")
                assertThat(followButtonUiState).isEqualTo(
                    ButtonUiState(
                        label = stringManager[R.string.join],
                        style = ButtonUiState.Style.Primary,
                    )
                )
            }
        }
    }

    private fun makePresenter(
        args: HashTagFeedScreen = HashTagFeedScreen(hashTag = HashTag.parse("test")),
    ): HashTagScreenPresenter {
        return HashTagScreenPresenter(
            feedUiProducer = { _, _ -> FeedUiState.Empty },
            observePagedHashTagFeed = ObservePagedHashTagFeed(noteRepository, FakeLastRequestDao()),
            observeFollowedHashTags = ObserveFollowedHashTags(
                contactsRepository,
                FakeAccountStateRepository()
            ),
            syncHashTagEvents = SyncHashTagEvents(
                relay = FakeRelayManager(),
                storeEvents = StoreEvents(
                    ioDispatcher = EmptyCoroutineContext,
                    eventsDao = FakeEventsDao()
                ),
                ioDispatcher = EmptyCoroutineContext,
                lastRequestDao = FakeLastRequestDao()
            ),
            stringManager = stringManager,
            args = args,
            navigator = navigator,
            followHashTag = FollowHashTag(contactsRepository),
            unfollowHashTag = UnfollowHashTag(contactsRepository),
        )
    }
}


