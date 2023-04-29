package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.models.TagSuggestion
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.fakes.FakeNip5Validator
import social.plasma.shared.repositories.fakes.FakeUserMetadataRepository
import kotlin.coroutines.EmptyCoroutineContext


@OptIn(ExperimentalCoroutinesApi::class)
class GetUserSuggestionsTest {
    private val userMetadataRepository = FakeUserMetadataRepository()
    private val getUserSuggestions = GetUserSuggestions(
        userMetadataRepository, GetNip5Status(
            FakeNip5Validator(), EmptyCoroutineContext
        )
    )

    @Test
    fun `when last word starts with @, model contains tag suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        val noteContent = "j"
        getUserSuggestions.apply {
            invoke(
                GetUserSuggestions.Params(noteContent)
            )
            flow.test {
                assertThat(awaitItem()).containsExactly(
                    TagSuggestion(
                        pubKey = PubKey.parse("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"),
                        imageUrl = null,
                        title = "test",
                        nip5Identifier = null,
                        isNip5Valid = null,
                    )
                )
            }
        }
    }

    @Test
    fun `when first word starts with @, model contains tag suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        getUserSuggestions.apply {
            invoke(GetUserSuggestions.Params("@j"))
            flow.test {
                assertThat(awaitItem()).containsExactly(
                    TagSuggestion(
                        pubKey = PubKey.parse("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"),
                        imageUrl = null,
                        title = "test",
                        nip5Identifier = null,
                        isNip5Valid = null,
                    )
                )
            }
        }
    }

    @Test
    fun `when query is empty, model emits empty suggestions`() = runTest {
        getUserSuggestions.apply {
            invoke(GetUserSuggestions.Params(""))
            flow.test {
                assertThat(awaitItem()).isEmpty()
            }
        }
    }

    private fun createUserMetadata() = UserMetadataEntity(
        name = "test",
        displayName = "",
        about = null,
        createdAt = null,
        banner = null,
        pubkey = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
        website = null,
        lud = null,
        nip05 = null,
        picture = null,
    )
}

