package social.plasma.domain.interactors

import social.plasma.domain.ResultInteractor
import social.plasma.models.PubKey
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class GetNoteTagSuggestions @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
) : ResultInteractor<GetNoteTagSuggestions.Params, List<TagSuggestion>>() {
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
                    pubKey = PubKey(it.pubkey),
                    imageUrl = it.picture,
                    title = it.userFacingName,
                    nip5Identifier = it.nip05,
                )
            }
        }

        return emptyList()
    }
}

