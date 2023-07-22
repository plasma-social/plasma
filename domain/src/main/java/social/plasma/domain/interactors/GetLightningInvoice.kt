package social.plasma.domain.interactors

import app.cash.nostrino.crypto.Bech32Serde
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import social.plasma.domain.ResultInteractor
import social.plasma.models.BitcoinAmount
import social.plasma.models.TipAddress
import social.plasma.models.TipAmount
import social.plasma.shared.repositories.api.LightningAddressResolver
import social.plasma.shared.repositories.api.LightningInvoiceFetcher
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

/**
 * Interactor that resolves a lightning invoice from a lightning address or lnurl.
 */
class GetLightningInvoice @Inject constructor(
    private val lightningAddressResolver: LightningAddressResolver,
    private val lightningInvoiceFetcher: LightningInvoiceFetcher,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : ResultInteractor<GetLightningInvoice.Params, Result<GetLightningInvoice.LightningInvoice>>() {
    override suspend fun doWork(params: Params): Result<LightningInvoice> =
        withContext(ioDispatcher) {
            when (val tipAddress = params.tipAddress) {
                is TipAddress.LightningAddress -> getFromLightningAddress(tipAddress, params.amount)
                is TipAddress.Lnurl -> getFromLnurl(tipAddress, params.amount)
            }
        }

    private suspend fun getFromLnurl(
        tipAddress: TipAddress.Lnurl,
        amount: TipAmount,
    ): Result<LightningInvoice> = runCatching {
        val (hrp, data, _) = Bech32Serde.decodeBytes(tipAddress.bech32)

        if (!hrp.equals("lnurl", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid bech32 hrp: $hrp")
        }

        val requestUrl = String(data, Charsets.UTF_8).toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid bech32 data")

        val resolvedLightningAddress = lightningAddressResolver.resolve(requestUrl)

        val invoiceResponse = lightningInvoiceFetcher.fetch(
            url = resolvedLightningAddress.callback,
            millisats = when (amount) {
                is BitcoinAmount -> amount.millisats
            }
        )

        LightningInvoice(invoiceResponse.paymentRequest)
    }

    private suspend fun getFromLightningAddress(
        lightningAddress: TipAddress.LightningAddress,
        amount: TipAmount,
    ): Result<LightningInvoice> =
        runCatching {
            val (username, domain) = lightningAddress.address.split("@")

            if (username.isBlank() || domain.isBlank()) {
                throw IllegalArgumentException("Invalid lightning address")
            }

            val resolvedLightningAddress = lightningAddressResolver.resolve(
                HttpUrl.Builder()
                    .scheme("https")
                    .host(domain)
                    .addPathSegment(".well-known")
                    .addPathSegment("lnurlp")
                    .addPathSegment(username)
                    .build()
            )

            val invoiceResponse = lightningInvoiceFetcher.fetch(
                resolvedLightningAddress.callback,
                when (amount) {
                    is BitcoinAmount -> amount.millisats
                }
            )

            LightningInvoice(invoiceResponse.paymentRequest)
        }

    data class Params(
        val tipAddress: TipAddress,
        val amount: TipAmount,
    )

    data class LightningInvoice(val invoice: String)
}
