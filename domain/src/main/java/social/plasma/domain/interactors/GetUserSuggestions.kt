package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Nip5Status
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class GetUserSuggestions @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
    private val getNip5Status: GetNip5Status,
    @Named("default") private val coroutineContext: CoroutineContext,
) : SubjectInteractor<GetUserSuggestions.Params, List<TagSuggestion>>() {
    data class Params(val query: String)

    private var currentJob: Job? = null

    override fun createObservable(params: Params): Flow<List<TagSuggestion>> {
        currentJob?.cancel() // Cancel any ongoing operations from the previous query
        val tagSuggestionsChannel = Channel<List<TagSuggestion>>(onBufferOverflow = DROP_OLDEST)

        currentJob = CoroutineScope(coroutineContext).launch {
            val query = if (params.query.startsWith("@")) params.query.drop(1) else params.query

            if (query.isNotEmpty()) {
                val userEntities = userMetadataRepository.searchUsers(query)

                val tagSuggestions = userEntities.map { userEntity ->
                    TagSuggestion(
                        pubKey = PubKey(userEntity.pubkey.decodeHex()),
                        imageUrl = userEntity.picture,
                        title = userEntity.userFacingName,
                        nip5Identifier = userEntity.nip05,
                        nip5Status = userEntity.nip05?.takeIf { it.isNotBlank() }
                            ?.let { Nip5Status.Set.Loading(it) } ?: Nip5Status.Missing
                    )
                }

                tagSuggestionsChannel.sendWithNip5Validation(tagSuggestions)
            } else {
                tagSuggestionsChannel.send(emptyList())
                tagSuggestionsChannel.close()
            }
        }

        return tagSuggestionsChannel.consumeAsFlow()
    }


    private suspend fun SendChannel<List<TagSuggestion>>.sendWithNip5Validation(tagSuggestions: List<TagSuggestion>) {
        send(tagSuggestions)

        val tagSuggestionsMap = tagSuggestions.associateBy { it.pubKey }.toMutableMap()

        coroutineScope {
            tagSuggestionsMap.values.forEach { suggestion ->
                launch {
                    val nip5Identifier = suggestion.nip5Identifier ?: return@launch

                    if (nip5Identifier.trim().isEmpty()) return@launch

                    val nip5Status = getNip5Status.executeSync(
                        GetNip5Status.Params(
                            suggestion.pubKey,
                            nip5Identifier
                        )
                    )

                    synchronized(tagSuggestionsMap) {
                        tagSuggestionsMap[suggestion.pubKey] =
                            suggestion.copy(nip5Status = nip5Status)
                    }

                    send(tagSuggestionsMap.values.toList())
                }
            }
        }
    }
}



