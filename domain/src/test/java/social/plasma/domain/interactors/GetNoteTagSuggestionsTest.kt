package social.plasma.domain.interactors

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.models.PubKey
import social.plasma.models.TagSuggestion
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.fakes.FakeUserMetadataRepository


@OptIn(ExperimentalCoroutinesApi::class)
class GetNoteTagSuggestionsTest {
    private val userMetadataRepository = FakeUserMetadataRepository()
    private val getNoteTagSuggestions = GetNoteTagSuggestions(userMetadataRepository)

    @Test
    fun `when last word starts with @, model contains tag suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        val noteContent = "Tagging \n @j"
        getNoteTagSuggestions(
            GetNoteTagSuggestions.Params(
                noteContent,
                cursorPosition = noteContent.length
            )
        ).test {
            assertThat(awaitItem()).containsExactly(
                TagSuggestion(
                    pubKey = PubKey(""),
                    imageUrl = null,
                    title = "test",
                    nip5Identifier = null
                )
            )
            awaitComplete()
        }
    }

    @Test
    fun `when first word starts with @, model contains tag suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        getNoteTagSuggestions(GetNoteTagSuggestions.Params("@j", cursorPosition = 2)).test {
            assertThat(awaitItem()).containsExactly(
                TagSuggestion(
                    pubKey = PubKey(""),
                    imageUrl = null,
                    title = "test",
                    nip5Identifier = null
                )
            )
            awaitComplete()
        }
    }

    @Test
    fun `when the @ is on a new line, model contains suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        val noteContent = "fsfds \n@j"
        getNoteTagSuggestions(
            GetNoteTagSuggestions.Params(
                noteContent,
                cursorPosition = noteContent.length
            )
        ).test {
            assertThat(awaitItem()).containsExactly(
                TagSuggestion(
                    pubKey = PubKey(""),
                    imageUrl = null,
                    title = "test",
                    nip5Identifier = null
                )
            )
            awaitComplete()
        }
    }

    @Test
    fun `when note contains a mention, model does not contain suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        val noteContent = "fsfds @jm fsdf"
        getNoteTagSuggestions(GetNoteTagSuggestions.Params(noteContent, noteContent.length)).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `when @ sign is within a word, model does not contain suggestions`() = runTest {
        userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

        val noteContent = "fdfd@fdfd"
        getNoteTagSuggestions(GetNoteTagSuggestions.Params(noteContent, noteContent.length)).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `when cursor position is after mention that's encountered within the text, model contains suggestions`() =
        runTest {
            userMetadataRepository.searchUsersResult = listOf(createUserMetadata())

            val noteContent = "fsfds @jm fsdf"
            getNoteTagSuggestions(GetNoteTagSuggestions.Params(noteContent, 9)).test {
                assertThat(awaitItem()).isNotEmpty()
                awaitComplete()
            }
        }


    private fun createUserMetadata() = UserMetadataEntity(
        name = "test",
        displayName = "",
        about = null,
        createdAt = null,
        banner = null,
        pubkey = "",
        website = null,
        lud = null,
        nip05 = null,
        picture = null,
    )
}

