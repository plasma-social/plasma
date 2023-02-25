package social.plasma.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealNip5ValidatorTest {
    private val server = MockWebServer()

    private val okHttpClient = OkHttpClient.Builder().build()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `valid nip5 returns true`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                "{" +
                        "  \"names\": {" +
                        "    \"jm\": \"9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab\"" +
                        "  }" +
                        "}"
            )
        )
        val repo = RealNip5Validator(
            okHttpClient = okHttpClient,
        )
        val httpUrl = server.url("/.well-known/nostr.json")
        val npub = "9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab"

        val isValid = repo.isValid(httpUrl, "jm", npub)

        assertThat(isValid).isTrue()
    }

    @Test
    fun `valid nip5 without spaces returns true`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                "{\"names\":{\"jm\":\"9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab\"}}"
            )
        )
        val repo = RealNip5Validator(
            okHttpClient = okHttpClient,
        )
        val httpUrl = server.url("/.well-known/nostr.json")
        val npub = "9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab"

        val isValid = repo.isValid(httpUrl, "jm", npub)

        assertThat(isValid).isTrue()
    }

    @Test
    fun `invalid nip5 returns false`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                "{" +
                        "  \"names\": {" +
                        "    \"jmax\": \"9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab\"" +
                        "  }" +
                        "}"
            )
        )
        val repo = RealNip5Validator(
            okHttpClient = okHttpClient,
        )

        val httpUrl = server.url("/.well-known/nostr.json")
        val npub = "9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab"

        val isValid = repo.isValid(httpUrl, "jm", npub)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `on http error nip5 returns false`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val repo = RealNip5Validator(
            okHttpClient = okHttpClient,
        )

        val httpUrl = server.url("/.well-known/nostr.json")
        val npub = "9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab"

        val isValid = repo.isValid(httpUrl, "jm", npub)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `on io exception nip5 returns false`() = runTest {
        server.enqueue(
            MockResponse().setBody("{\"names\":{\"jm\":\"9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab\"}}")
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )

        val repo = RealNip5Validator(
            okHttpClient = okHttpClient,
        )

        val httpUrl = server.url("/.well-known/nostr.json")
        val npub = "9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab"

        val isValid = repo.isValid(httpUrl, "jm", npub)

        assertThat(isValid).isFalse()
    }
}
