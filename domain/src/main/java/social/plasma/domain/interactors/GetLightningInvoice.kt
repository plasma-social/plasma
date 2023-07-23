package social.plasma.domain.interactors

import app.cash.nostrino.crypto.Bech32Serde
import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import social.plasma.domain.ResultInteractor
import social.plasma.models.BitcoinAmount
import social.plasma.models.NoteId
import social.plasma.models.TipAddress
import social.plasma.models.TipAmount
import social.plasma.nostr.models.ZapRequestProvider
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.LightningAddressResolver
import social.plasma.shared.repositories.api.LightningAddressResponse
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
    private val zapRequestProvider: ZapRequestProvider,
    private val accountStateRepository: AccountStateRepository,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : ResultInteractor<GetLightningInvoice.Params, Result<GetLightningInvoice.LightningInvoice>>() {

    override suspend fun doWork(params: Params): Result<LightningInvoice> =
        withContext(ioDispatcher) {
            when (val tipAddress = params.tipAddress) {
                is TipAddress.LightningAddress -> getFromLightningAddress(tipAddress, params)
                is TipAddress.Lnurl -> getFromLnurl(tipAddress, params)
            }
        }

    private suspend fun getFromLnurl(
        tipAddress: TipAddress.Lnurl,
        params: Params,
    ): Result<LightningInvoice> = runCatching {
        val (hrp, data, _) = Bech32Serde.decodeBytes(tipAddress.bech32)

        if (!hrp.equals("lnurl", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid bech32 hrp: $hrp")
        }

        val requestUrl = String(data, Charsets.UTF_8).toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid bech32 data")

        val lightningUrl = lightningAddressResolver.resolve(requestUrl)

        LightningInvoice(fetchInvoice(lightningUrl, params).invoice)
    }

    private suspend fun getFromLightningAddress(
        lightningAddress: TipAddress.LightningAddress,
        params: Params,
    ): Result<LightningInvoice> =
        runCatching {
            val (username, domain) = lightningAddress.address.split("@")

            if (username.isBlank() || domain.isBlank()) {
                throw IllegalArgumentException("Invalid lightning address")
            }

            val lightningUrl = lightningAddressResolver.resolve(
                HttpUrl.Builder()
                    .scheme("https")
                    .host(domain)
                    .addPathSegment(".well-known")
                    .addPathSegment("lnurlp")
                    .addPathSegment(username)
                    .build()
            )

            LightningInvoice(fetchInvoice(lightningUrl, params).invoice)
        }

    private suspend fun fetchInvoice(
        lightningUrlResponse: LightningAddressResponse,
        params: Params,
    ): LightningInvoice {
        val millisats = when (params.amount) {
            is BitcoinAmount -> params.amount.millisats
        }

        val url = if (lightningUrlResponse.allowsNostr && params.recipient != null) {
            lightningUrlResponse.callback.toHttpUrl().newBuilder()
                .addEncodedQueryParameter(
                    "nostr",
                    zapRequestProvider.getSignedZapRequest(
                        secKey = SecKey(accountStateRepository.getSecretKey()!!.toByteString()),
                        recipient = params.recipient,
                        amount = millisats,
                        eventId = params.event?.hex?.decodeHex()
                    )
                )
                .build()
                .toString()
        } else {
            lightningUrlResponse.callback
        }

        val invoiceResponse = lightningInvoiceFetcher.fetch(url, millisats)

        return LightningInvoice(invoiceResponse.paymentRequest)
    }

    data class Params(
        val tipAddress: TipAddress,
        val amount: TipAmount,
        val event: NoteId? = null,
        val recipient: PubKey? = null,
    )

    data class LightningInvoice(val invoice: String)
}
