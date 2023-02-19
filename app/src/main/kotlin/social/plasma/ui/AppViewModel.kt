package social.plasma.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import social.plasma.models.PubKey
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
            userMetaDataRepository.syncUserMetadata(publicKey.hex)
            contactListRepository.syncContactList(publicKey.hex)
        } else {
            emptyFlow()
        }
    }
}
