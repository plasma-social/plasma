package social.plasma.domain.observers

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import okio.ByteString.Companion.toByteString
import social.plasma.domain.SubjectInteractor
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class ObserveCurrentUserMetadata @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
    private val accountStateRepository: AccountStateRepository,
) : SubjectInteractor<Unit, UserMetadataEntity?>() {

    override fun createObservable(params: Unit): Flow<UserMetadataEntity?> {
        return userMetadataRepository.observeUserMetaData(
            PubKey(accountStateRepository.getPublicKey()!!.toByteString())
        )
    }
}
