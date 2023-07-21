package social.plasma.shared.repositories.real

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import social.plasma.shared.repositories.api.LightningAddressResolver
import social.plasma.shared.repositories.api.LightningAddressResponse
import java.io.IOException
import javax.inject.Inject

class RealLightningAddressResolver @Inject constructor(
    private val okHttpClient: OkHttpClient,
    moshi: Moshi,
) : LightningAddressResolver {
    private val lightningAddressResponseAdapter: JsonAdapter<LightningAddressResponse> =
        moshi.adapter(LightningAddressResponse::class.java)

    // TODO cache resolved lightning addresses
    override suspend fun resolve(
        httpUrl: HttpUrl,
    ): LightningAddressResponse {
        require(httpUrl.pathSegments.take(2) == listOf(".well-known", "lnurlp"))
        val request = Request.Builder().url(httpUrl).build()

        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            throw e
        }

        if (!response.isSuccessful) {
            throw IOException("Unsuccessful lightning request")
        }

        val responseBody = try {
            lightningAddressResponseAdapter.fromJson(response.body.source())
        } catch (e: JsonDataException) {
            throw IOException("Invalid lightning response")
        }

        return responseBody ?: throw IOException("Unable to parse lightning address response")
    }

}
