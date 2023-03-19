package social.plasma.shared.repositories.real

import androidx.collection.LruCache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import social.plasma.shared.repositories.api.Nip5Validator
import java.io.IOException
import javax.inject.Inject

internal class RealNip5Validator @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : Nip5Validator {
    private val cache = LruCache<String, String>(500)
    override suspend fun isValid(serverUrl: HttpUrl, name: String, pubKeyHex: String): Boolean {
        // TODO prevent duplicate requests if request is in-flight
        val url = serverUrl.newBuilder().addQueryParameter(QUERY_NAME, name).build()

        if (cache[url.toString()] == pubKeyHex) return true

        val request = Request.Builder()
            .url(url).get()
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()

            response.body.string().replace(" ", "").contains("\"$name\":\"$pubKeyHex\"")
                .also { isValid ->
                    if (isValid) {
                        cache.put(url.toString(), pubKeyHex)
                    }
                }
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        private const val QUERY_NAME = "name"
    }
}