package social.plasma.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import social.plasma.db.EventStore
import social.plasma.models.PubKey
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.UserMetaDataRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    accountStateRepository: AccountStateRepository,
    userMetaDataRepository: UserMetaDataRepository,
    eventStore: EventStore,
) : ViewModel() {
    private val isLoggedIn = accountStateRepository.isLoggedIn.distinctUntilChanged()

    init {
        viewModelScope.launch {
            eventStore.sync()
        }
    }

    val syncGlobalData = isLoggedIn.flatMapLatest { loggedIn ->
        if (loggedIn) {
            val publicKey = PubKey.of(accountStateRepository.getPublicKey()!!)
            userMetaDataRepository.syncUserMetadata(publicKey.hex)
            accountStateRepository.syncMyData()
        } else {
            emptyFlow()
        }
    }
}
