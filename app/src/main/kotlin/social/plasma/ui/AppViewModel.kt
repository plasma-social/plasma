package social.plasma.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import social.plasma.PubKey
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.ContactListRepository
import social.plasma.repository.UserMetaDataRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    accountStateRepository: AccountStateRepository,
    contactListRepository: ContactListRepository,
    userMetaDataRepository: UserMetaDataRepository,
) : ViewModel() {
    private val isLoggedIn = accountStateRepository.isLoggedIn.distinctUntilChanged()

    val syncGlobalData = isLoggedIn.flatMapLatest { loggedIn ->
        if (loggedIn) {
            val publicKey = PubKey.of(accountStateRepository.getPublicKey()!!)
            merge(
                contactListRepository.syncContactList(publicKey.hex),
                contactListRepository.observeContactLists(publicKey.hex)
                    .distinctUntilChanged()
                    .filter { it.isNotEmpty() }
                    .flatMapLatest { contacts ->
                        val pubKeys = contacts.map { it.pubKey.hex() }
                        userMetaDataRepository.syncUserMetadata(setOf(publicKey.hex) + pubKeys)
                    }
            )
        } else {
            emptyFlow()
        }
    }
}
