package social.plasma.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import social.plasma.data.daos.ContactsDao
import social.plasma.domain.Interactor
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Contact
import social.plasma.models.ContactEntity
import social.plasma.models.Event
import java.time.Instant
import javax.inject.Inject

class StoreContactList @Inject constructor(
    private val contactListDao: ContactsDao,
) : SubjectInteractor<Flow<Event>, Any>() {
    override fun createObservable(params: Flow<Event>): Flow<Any> {
        return params.filter { it.kind == Event.Kind.ContactList }
            .map { it.typed(it.typed("").contacts()) }
            .filterNotNull()
            .distinctUntilChanged()
            .map {
                it.pubKey.hex() to it.contacts()
            }
            .onEach { (owner, contacts) ->
                contactListDao.insert(contacts.map { it.toContactEntity(owner) })
            }
    }
}

private fun Contact.toContactEntity(owner: String) = ContactEntity(
    owner = owner,
    pubKey = pubKey.hex(),
    petName = petName,
    homeRelay = homeRelayUrl,
    createdAt = Instant.now().epochSecond,
)