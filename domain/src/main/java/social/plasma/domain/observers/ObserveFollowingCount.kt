package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.SubjectInteractor
import app.cash.nostrino.crypto.PubKey
import javax.inject.Inject

class ObserveFollowingCount @Inject constructor(
    private val contactsDao: ContactsDao
) : SubjectInteractor<PubKey, Long>() {
    override fun createObservable(params: PubKey): Flow<Long> {
        return contactsDao.observeFollowingCount(params.key.hex())
    }
}