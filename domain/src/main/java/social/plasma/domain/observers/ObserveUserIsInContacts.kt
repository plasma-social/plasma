package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.PubKey
import javax.inject.Inject

class ObserveUserIsInContacts @Inject constructor(
    private val contactListDao: ContactsDao,
) : SubjectInteractor<ObserveUserIsInContacts.Params, Boolean>() {
    data class Params(val ownerPubKey: PubKey, val contactPubKey: PubKey)

    override fun createObservable(params: Params): Flow<Boolean> {
        return contactListDao.observeOwnerFollowsContact(
            ownerPubKey = params.ownerPubKey.hex,
            contactPubKey = params.contactPubKey.hex
        )
    }
}