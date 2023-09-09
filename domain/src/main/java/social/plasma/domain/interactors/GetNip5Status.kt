package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import social.plasma.domain.ResultInteractor
import social.plasma.models.Nip5Status
import social.plasma.shared.repositories.api.Nip5Validator
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class GetNip5Status @Inject constructor(
    private val nip5Validator: Nip5Validator,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : ResultInteractor<GetNip5Status.Params, Nip5Status>() {
    data class Params(val pubKey: PubKey, val identifier: String?)

    override suspend fun doWork(params: Params): Nip5Status = withContext(ioDispatcher) {
        params.identifier ?: return@withContext Nip5Status.Missing

        val parts = params.identifier.split("@")

        if (parts.size != 2) {
            return@withContext Nip5Status.Set.Invalid(params.identifier)
        }

        val name = parts[0]
        val domain = parts[1]

        val pubKeyHex = params.pubKey.key.hex()
        val httpUrl = try {
            HttpUrl.Builder()
                .scheme("https")
                .host(domain)
                .encodedPath("/.well-known/nostr.json")
                .build()
        } catch (e: IllegalArgumentException) {
            return@withContext Nip5Status.Set.Invalid(params.identifier)
        }

        val isValid = nip5Validator.isValid(serverUrl = httpUrl, name = name, pubKeyHex = pubKeyHex)

        if (isValid) {
            return@withContext Nip5Status.Set.Valid(params.identifier)
        } else {
            return@withContext Nip5Status.Set.Invalid(params.identifier)
        }
    }
}

