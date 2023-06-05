package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.toByteString
import social.plasma.domain.SubjectInteractor
import social.plasma.models.HashTag
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.ContactsRepository
import javax.inject.Inject

class ObserveFollowedHashTags @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val accountStateRepository: AccountStateRepository,
) : SubjectInteractor<Unit, List<HashTag>>() {
    override fun createObservable(params: Unit): Flow<List<HashTag>> {
        return contactsRepository.observeContactListEvent(
            accountStateRepository.getPublicKey()!!.toByteString().hex()
        ).distinctUntilChanged().filterNotNull().map { it.tags }
            .map { tagList -> tagList.filter { tag -> tag.size >= 2 && tag[0] == "t" } }
            .map { tagList ->
                tagList.map { tag ->
                    HashTag.parse(tag[1])
                }
            }
    }
}
