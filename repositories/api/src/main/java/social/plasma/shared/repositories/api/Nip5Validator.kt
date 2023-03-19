package social.plasma.shared.repositories.api

import okhttp3.HttpUrl

interface Nip5Validator {
    suspend fun isValid(serverUrl: HttpUrl, name: String, pubKeyHex: String): Boolean
}

