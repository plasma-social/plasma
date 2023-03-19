package social.plasma.domain.observers

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import social.plasma.data.daos.UserMetadataDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.PubKey
import social.plasma.models.UserMetadataEntity
import javax.inject.Inject

class ObserveUserMetadata @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
) : SubjectInteractor<ObserveUserMetadata.Params, UserMetadataEntity?>() {

    override fun createObservable(params: Params): Flow<UserMetadataEntity?> {
        return userMetadataDao.observeUserMetadata(params.pubKey.hex).onEach {
            Log.d("@@@", "$it")
        }
    }

    data class Params(val pubKey: PubKey)
}