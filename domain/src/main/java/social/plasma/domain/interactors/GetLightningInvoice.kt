package social.plasma.domain.interactors

import app.cash.nostrino.crypto.Bech32Serde
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import social.plasma.domain.ResultInteractor
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
            when (params) {
                is Params.LightningAddress -> getFromLightningAddress(params)
                is Params.Lnurl -> getFromLnurl(params)
            }
        }

    private suspend fun getFromLnurl(params: Params.Lnurl): Result<LightningInvoice> = runCatching {
        val (hrp, data, _) = Bech32Serde.decodeBytes(params.bech32)

        if (!hrp.equals("lnurl", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid bech32 hrp: $hrp")
        }

        val requestUrl = String(data, Charsets.UTF_8).toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid bech32 data")

        val resolvedLightningAddress = lightningAddressResolver.resolve(requestUrl)

        val invoiceResponse =
            lightningInvoiceFetcher.fetch(resolvedLightningAddress.callback, params.amount)

        LightningInvoice(invoiceResponse.paymentRequest)
    }

    private suspend fun getFromLightningAddress(params: Params.LightningAddress): Result<LightningInvoice> =
        runCatching {
            val (username, domain) = params.address.split("@")

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
                params.amount
            )

            LightningInvoice(invoiceResponse.paymentRequest)
        }

    sealed interface Params {
        val amount: Long

        data class LightningAddress(
            val address: String,
            override val amount: Long,
        ) : Params

        data class Lnurl(
            val bech32: String,
            override val amount: Long,
        ) : Params
    }

    data class LightningInvoice(val invoice: String)
}
