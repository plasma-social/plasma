package social.plasma.shared.repositories.fakes

import app.cash.turbine.Turbine
import okhttp3.HttpUrl
import social.plasma.shared.repositories.api.LightningAddressResolver
import social.plasma.shared.repositories.api.LightningAddressResponse

class FakeLightningAddressResolver : LightningAddressResolver {
    val lightningAddressResponses = Turbine<LightningAddressResponse>()
    val lightningAddressCalls = Turbine<HttpUrl>()
    override suspend fun resolve(
        httpUrl: HttpUrl,
    ): LightningAddressResponse {
        lightningAddressCalls.add(httpUrl)
        return lightningAddressResponses.awaitItem()
    }
}
