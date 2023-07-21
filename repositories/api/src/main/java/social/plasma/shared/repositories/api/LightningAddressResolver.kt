package social.plasma.shared.repositories.api

import com.squareup.moshi.JsonClass
import okhttp3.HttpUrl

interface LightningAddressResolver {
    suspend fun resolve(
        httpUrl: HttpUrl,
    ): LightningAddressResponse
}

@JsonClass(generateAdapter = true)
data class LightningAddressResponse(
    val callback: String,
    val maxSendable: Long,
    val minSendable: Long,
)
