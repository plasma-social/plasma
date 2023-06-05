package social.plasma.domain.observers

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import social.plasma.data.daos.HashtagDao
import social.plasma.domain.SubjectInteractor
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.models.HashTag
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class ObserveCommunityAvatars @Inject constructor(
    private val hashtagDao: HashtagDao,
    private val syncUserMetadata: SyncMetadata,
    private val userMetadataRepository: UserMetadataRepository,
) : SubjectInteractor<ObserveCommunityAvatars.Params, List<String>>() {
    data class Params(val limit: Int, val hashTag: HashTag)

    // Returns a flow of picture urls from the user metadata repository for each unique pubkey
    override fun createObservable(params: Params): Flow<List<String>> = flow {
        val uniquePubkeys =
            hashtagDao.getCommunityLatestPubkeys(
                hashTag = params.hashTag.name,
                limit = params.limit
            ).map { PubKey(it.decodeHex()) }

        // Sync the user metadata for each unique pubkey
        syncUserMetadataForPubkeys(uniquePubkeys)

        // Get the picture urls from the user metadata repository
        val pictureUrls = uniquePubkeys.map { pubkey ->
            userMetadataRepository.observeUserMetaData(pubkey)
                .map { it?.picture }
        }

        // Emit the picture urls whenever any of them change
        combine(pictureUrls) { it.filterNotNull() }.collect { emit(it) }
    }

    private suspend fun syncUserMetadataForPubkeys(pubkeys: List<PubKey>) {
        pubkeys.forEach { pubkey ->
            coroutineScope {
                launch {
                    syncUserMetadata.executeSync(SyncMetadata.Params(pubkey))
                }
            }
        }
    }
}

