package social.plasma.shared.repositories.fakes

import app.cash.turbine.Turbine
import social.plasma.shared.repositories.api.LightningInvoiceFetcher
import social.plasma.shared.repositories.api.LightningInvoiceResponse

class FakeLightningInvoiceFetcher : LightningInvoiceFetcher {
    val lightningInvoiceResponses = Turbine<LightningInvoiceResponse>()
    val lightningInvoiceCalls = Turbine<Pair<String, Long>>()

    override suspend fun fetch(url: String, millisats: Long): LightningInvoiceResponse {
        lightningInvoiceCalls.add(url to millisats)
        return lightningInvoiceResponses.awaitItem()
    }
}
