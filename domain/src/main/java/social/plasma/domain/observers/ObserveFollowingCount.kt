package social.plasma.domain.observers

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.SubjectInteractor
import javax.inject.Inject

class ObserveFollowingCount @Inject constructor(
    private val contactsDao: ContactsDao,
) : SubjectInteractor<PubKey, Long>() {
    override fun createObservable(params: PubKey): Flow<Long> {
        return contactsDao.observeContactListEvent(params.hex()).filterNotNull()
            .map { contactEvent ->
                contactEvent.tags.count { it.isNotEmpty() && it[0] == "p" }.toLong()
            }
    }
}
