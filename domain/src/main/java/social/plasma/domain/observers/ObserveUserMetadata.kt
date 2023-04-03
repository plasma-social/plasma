package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import social.plasma.domain.SubjectInteractor
import app.cash.nostrino.crypto.PubKey
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class ObserveUserMetadata @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
) : SubjectInteractor<ObserveUserMetadata.Params, UserMetadataEntity?>() {

    override fun createObservable(params: Params): Flow<UserMetadataEntity?> {
        return userMetadataRepository.observeUserMetaData(params.pubKey)
    }

    data class Params(val pubKey: PubKey)
}
