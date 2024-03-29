package social.plasma.shared.repositories.real

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Test
import social.plasma.shared.repositories.api.LightningAddressResponse
import java.io.IOException


class RealLightningUrlResolverTest {

    private val server = MockWebServer()
    private val okHttpClient = OkHttpClient.Builder().build()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val MockWebServer.scheme: String
        get() = "http"

    private val lightningUrlResolver = RealLightningUrlResolver(
        okHttpClient = okHttpClient,
        moshi = moshi,
    )

    private val httpUrl = HttpUrl.Builder()
        .scheme(server.scheme)
        .port(server.port)
        .host(server.hostName)
        .addPathSegment(".well-known")
        .addPathSegment("lnurlp")
        .addPathSegment("test")
        .build()

    @Test
    fun `resolves a lightning address for a service that supports nostr`() = runTest {
        server.enqueue(MockResponse().setBody(lightningUrlResponseWithNostr))

        val result = lightningUrlResolver.resolve(httpUrl)

        assertThat(server.takeRequest().path).isEqualTo("/.well-known/lnurlp/test")
        assertThat(result).isEqualTo(
            LightningAddressResponse(
                callback = testLnurl,
                maxSendable = 100000000000,
                minSendable = 1000,
                commentAllowed = 32,
                allowsNostr = true,
                nostrPubkey = "be1d89794bf92de5dd64c1e60f6a2c70c140abac9932418fee30c5c637fe9479"
            )
        )
    }

    @Test
    fun `resolves a lightning url for a service that does not support nostr`() = runTest {
        server.enqueue(MockResponse().setBody(lightningUrlResponseWithoutNostr))

        val result = lightningUrlResolver.resolve(httpUrl)

        assertThat(server.takeRequest().path).isEqualTo("/.well-known/lnurlp/test")
        assertThat(result).isEqualTo(
            LightningAddressResponse(
                callback = testLnurl,
                maxSendable = 100000000000,
                minSendable = 1000,
                commentAllowed = 32,
                allowsNostr = false,
                nostrPubkey = null
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid lightning address returns a failure`() = runTest {
        lightningUrlResolver.resolve(
            HttpUrl.Builder().scheme("http").host(server.hostName).build()
        )
    }

    @Test(expected = IOException::class)
    fun `when lightning address resolver call fails, returns a failure`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

        lightningUrlResolver.resolve(httpUrl)
    }

    @Test(expected = IOException::class)
    fun `when lightning address resolver call returns a non-200, returns a failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        lightningUrlResolver.resolve(httpUrl)
    }

    @Test(expected = JsonEncodingException::class)
    fun `when lightning address resolver returns malformed json, returns a failure`() = runTest {
        server.enqueue(MockResponse().setBody("not json"))

        lightningUrlResolver.resolve(httpUrl)
    }

    companion object {
        const val testLnurl = "api.plasma.social"
        const val lightningUrlResponseWithoutNostr =
            "{\"callback\":\"$testLnurl\",\"maxSendable\":100000000000,\"minSendable\":1000,\"metadata\":\"[[\\\"text/plain\\\",\\\"Pay to Wallet of Satoshi user: uglychurch65\\\"],[\\\"text/identifier\\\",\\\"uglychurch65@walletofsatoshi.com\\\"]]\",\"commentAllowed\":32,\"tag\":\"payRequest\"}"
        const val lightningUrlResponseWithNostr =
            "{\"callback\":\"$testLnurl\",\"maxSendable\":100000000000,\"minSendable\":1000,\"metadata\":\"[[\\\"text/plain\\\",\\\"Pay to Wallet of Satoshi user: uglychurch65\\\"],[\\\"text/identifier\\\",\\\"uglychurch65@walletofsatoshi.com\\\"]]\",\"commentAllowed\":32,\"tag\":\"payRequest\",\"allowsNostr\":true,\"nostrPubkey\":\"be1d89794bf92de5dd64c1e60f6a2c70c140abac9932418fee30c5c637fe9479\"}"
    }
}
