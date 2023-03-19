package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import social.plasma.domain.SubjectInteractor
import social.plasma.models.UserMetadata
import javax.inject.Inject

class ObserveLoggedInUserMetadata @Inject constructor(
//    userMetadataDao: UserMetadataDao,
) : SubjectInteractor<Unit, UserMetadata>() {
    override fun createObservable(params: Unit): Flow<UserMetadata> {
        return emptyFlow()
    }
}