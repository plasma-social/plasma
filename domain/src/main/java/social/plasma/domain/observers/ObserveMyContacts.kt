package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.ContactEntity
import social.plasma.models.PubKey
import javax.inject.Inject

class ObserveContacts @Inject constructor(
    private val contactsDao: ContactsDao,
) : SubjectInteractor<ObserveContacts.Params, List<ContactEntity>>() {
    override fun createObservable(params: Params): Flow<List<ContactEntity>> {
        return contactsDao.observeContacts(params.pubKey.hex)
    }

    data class Params(val pubKey: PubKey)
}
