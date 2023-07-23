package social.plasma.shared.repositories.real

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import social.plasma.shared.repositories.api.LightningInvoiceFetcher
import social.plasma.shared.repositories.api.LightningInvoiceResponse
import java.io.IOException
import javax.inject.Inject

class RealLightningInvoiceFetcher @Inject constructor(
    private val okHttpClient: OkHttpClient,
    moshi: Moshi,
) : LightningInvoiceFetcher {
    private val lightningInvoiceResponseAdapter =
        moshi.adapter(LightningInvoiceResponse::class.java)

    override suspend fun fetch(
        url: String,
        millisats: Long,
    ): LightningInvoiceResponse {
        val serviceUrl = try {
            url.toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("amount", millisats.toString())
                .build()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid lnurl address")
        } catch (e: NullPointerException) {
            throw IllegalArgumentException("Invalid lnurl address")
        }

        val request = Request.Builder().url(serviceUrl).build()

        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            throw e
        }

        if (!response.isSuccessful) {
            throw IOException("Unsuccessful lnurl response")
        }

        val responseBody = try {
            lightningInvoiceResponseAdapter.fromJson(response.body.source())
        } catch (e: JsonDataException) {
            throw IOException("Invalid lnurl response")
        }

        return responseBody ?: throw IOException("Unable to parse lnurl response")
    }
}
