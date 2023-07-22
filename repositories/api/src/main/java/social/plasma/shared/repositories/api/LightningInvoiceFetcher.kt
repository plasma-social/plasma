package social.plasma.shared.repositories.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface LightningInvoiceFetcher {
    suspend fun fetch(url: String, millisats: Long): LightningInvoiceResponse
}

@JsonClass(generateAdapter = true)
data class LightningInvoiceResponse(
    @Json(name = "pr")
    val paymentRequest: String,
)
