package social.plasma.domain.interactors

import app.cash.nostrino.crypto.Bech32Serde
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.ByteString.Companion.toByteString
import org.junit.Test
import social.plasma.domain.interactors.GetLightningInvoice.LightningInvoice
import social.plasma.models.BitcoinAmount
import social.plasma.models.TipAddress
import social.plasma.shared.repositories.api.LightningAddressResponse
import social.plasma.shared.repositories.api.LightningInvoiceResponse
import social.plasma.shared.repositories.fakes.FakeLightningAddressResolver
import social.plasma.shared.repositories.fakes.FakeLightningInvoiceFetcher
import kotlin.coroutines.EmptyCoroutineContext

class GetLightningInvoiceTest {
    private val lightningInvoiceFetcher = FakeLightningInvoiceFetcher()
    private val lightningAddressResolver = FakeLightningAddressResolver()

    private val interactor = GetLightningInvoice(
        lightningAddressResolver = lightningAddressResolver,
        lightningInvoiceFetcher = lightningInvoiceFetcher,
        ioDispatcher = EmptyCoroutineContext,
    )

    @Test
    fun `retrieves a lightning invoice using a lightning address`() {
        runTest {
            lightningAddressResolver.lightningAddressResponses.add(
                validLightningAddressResponse
            )
            lightningInvoiceFetcher.lightningInvoiceResponses.add(
                validInvoiceResponse
            )

            val result =
                interactor.executeSync(
                    GetLightningInvoice.Params(
                        tipAddress = TipAddress.LightningAddress("test@plasma.social.com"),
                        amount = BitcoinAmount(1000L),
                    )
                )

            assertThat(lightningAddressResolver.lightningAddressCalls.awaitItem())
                .isEqualTo("https://plasma.social.com/.well-known/lnurlp/test".toHttpUrlOrNull())
            assertThat(lightningInvoiceFetcher.lightningInvoiceCalls.awaitItem())
                .isEqualTo(TEST_LIGHTNING_URL to 1000_000L)
            assertThat(result.getOrNull()).isEqualTo(LightningInvoice(TEST_LIGHTNING_INVOICE))
        }
    }

    @Test
    fun `resolves a lightning invoice using a LNURL`() = runTest {
        lightningAddressResolver.lightningAddressResponses.add(
            validLightningAddressResponse
        )
        lightningInvoiceFetcher.lightningInvoiceResponses.add(
            validInvoiceResponse
        )

        val result = interactor.executeSync(
            GetLightningInvoice.Params(
                TipAddress.Lnurl(bech32 = TEST_LNURL),
                amount = BitcoinAmount(1000L)
            )
        )

        assertThat(result.getOrNull()).isEqualTo(LightningInvoice(TEST_LIGHTNING_INVOICE))
        assertThat(lightningInvoiceFetcher.lightningInvoiceCalls.awaitItem())
            .isEqualTo(TEST_LIGHTNING_URL to 1000_000L)
        assertThat(lightningAddressResolver.lightningAddressCalls.awaitItem())
            .isEqualTo(TEST_LIGHTNING_URL.toHttpUrlOrNull())
    }

    @Test
    fun `invalid lnurl address returns a failure`() = runTest {
        val result = interactor.executeSync(
            GetLightningInvoice.Params(
                TipAddress.Lnurl(bech32 = "test"),
                amount = BitcoinAmount(1000L)
            )
        )

        assertThat(result.isFailure).isTrue()
    }


    companion object {
        private const val TEST_LIGHTNING_URL =
            "http://plasma.social/api/v1/lnurl/payreq/2dbc5cf8-85ef-4038-80a4-095391916f93"
        private val validLightningAddressResponse = LightningAddressResponse(
            callback = TEST_LIGHTNING_URL,
            minSendable = 1000L,
            maxSendable = 100000L,
        )
        private const val TEST_LIGHTNING_INVOICE = "lnbclkfjsdlkfjsdlkfjlsdkfkjlsdflkjdsalkf"
        private val validInvoiceResponse = LightningInvoiceResponse(
            paymentRequest = TEST_LIGHTNING_INVOICE
        )
        private val TEST_LNURL =
            Bech32Serde.encodeBytes(
                "lnurl",
                TEST_LIGHTNING_URL.toByteArray().toByteString(),
                encoding = Bech32Serde.Encoding.Bech32
            )
    }
}
