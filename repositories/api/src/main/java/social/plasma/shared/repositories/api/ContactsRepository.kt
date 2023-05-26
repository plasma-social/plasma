package social.plasma.shared.repositories.api

interface ContactsRepository {
    suspend fun follow(pubKeyHex: String)

    suspend fun unfollow(pubKeyHex: String)
}
