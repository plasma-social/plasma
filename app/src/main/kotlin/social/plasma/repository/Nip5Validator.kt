package social.plasma.repository

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

interface Nip5Validator {
    suspend fun isValid(serverUrl: HttpUrl, name: String, pubKeyHex: String): Boolean
}

internal class RealNip5Validator @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : Nip5Validator {
    override suspend fun isValid(serverUrl: HttpUrl, name: String, pubKeyHex: String): Boolean {
        // TODO prevent duplicate requests if request is in-flight
        val url = serverUrl.newBuilder().addQueryParameter(QUERY_NAME, name).build()

        val request = Request.Builder()
            .url(url).get()
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()

            response.body.string().replace(" ", "").contains("\"$name\":\"$pubKeyHex\"")
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        private const val QUERY_NAME = "name"
    }
}
