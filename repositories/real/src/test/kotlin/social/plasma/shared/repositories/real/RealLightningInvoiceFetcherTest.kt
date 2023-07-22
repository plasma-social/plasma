package social.plasma.shared.repositories.real

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Test
import social.plasma.shared.repositories.api.LightningInvoiceResponse
import java.io.IOException

class RealLightningInvoiceFetcherTest {
    private val server = MockWebServer()
    private val okHttpClient = OkHttpClient.Builder().build()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val lightningAddressFetcher = RealLightningInvoiceFetcher(
        okHttpClient = okHttpClient,
        moshi = moshi,
    )

    @Test
    fun `fetches a lightning invoice`() = runTest {
        server.enqueue(MockResponse().setBody(validInvoiceResponseJson))

        val result = lightningAddressFetcher.fetch(
            server.url("/").toString(),
            millisats = 1000,
        )

        assertThat(result).isEqualTo(validInvoiceResponse)
        assertThat(server.takeRequest().path).isEqualTo("/?amount=1000")
    }

    @Test(expected = IOException::class)
    fun `when lightning invoice call fails, returns a failure`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

        lightningAddressFetcher.fetch(
            server.url("/").toString(),
            millisats = 1000,
        )
    }

    @Test(expected = IOException::class)
    fun `when lightning invoice call returns a non-200, returns a failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        lightningAddressFetcher.fetch(
            server.url("/").toString(),
            millisats = 1000,
        )
    }

    @Test(expected = JsonEncodingException::class)
    fun `when lightning invoice call returns malformed json, returns a failure`() = runTest {
        server.enqueue(MockResponse().setBody("not json"))

        lightningAddressFetcher.fetch(
            server.url("/").toString(),
            millisats = 1000,
        )
    }

    companion object {
        private const val TEST_LIGHTNING_INVOICE = "lnbclkfjsdlkfjsdlkfjlsdkfkjlsdflkjdsalkf"
        private val validInvoiceResponse = LightningInvoiceResponse(
            paymentRequest = TEST_LIGHTNING_INVOICE
        )
        private const val validInvoiceResponseJson =
            "{\"pr\":\"$TEST_LIGHTNING_INVOICE\"}"
    }
}
