package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import okio.ByteString.Companion.decodeHex
import social.plasma.domain.ResultInteractor
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class GetUserSuggestions @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
) : ResultInteractor<GetUserSuggestions.Params, List<TagSuggestion>>() {
    data class Params(val noteContent: String, val cursorPosition: Int)

    override suspend fun doWork(params: Params): List<TagSuggestion> {
        if (params.cursorPosition <= 0) return emptyList()

        val contentBeforeCursor = params.noteContent.substring(0, params.cursorPosition)

        val query = contentBeforeCursor.substring(contentBeforeCursor.lastIndexOf(" ").inc())
            .replace("\n", "")

        if (query.startsWith("@") && query.length > 1) {
            val userEntities = userMetadataRepository.searchUsers(query.drop(1))

            return userEntities.map {
                TagSuggestion(
                    pubKey = PubKey(it.pubkey.decodeHex()),
                    imageUrl = it.picture,
                    title = it.userFacingName,
                    nip5Identifier = it.nip05,
                )
            }
        }

        return emptyList()
    }
}

