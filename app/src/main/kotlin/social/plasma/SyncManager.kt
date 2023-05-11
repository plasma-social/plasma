package social.plasma

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.SyncContactsEvents
import social.plasma.domain.interactors.SyncMyEvents
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject

@ActivityRetainedScoped
class SyncManager @Inject constructor(
    accountStateRepository: AccountStateRepository,
    private val syncMyEvents: SyncMyEvents,
    private val syncMyContactsEvents: SyncContactsEvents,
    private val coroutineScope: CoroutineScope,
) {

    private val isLoggedInFlow = accountStateRepository.isLoggedIn

    init {
        startSync()
    }

    private fun startSync() {
        coroutineScope.launch {
            isLoggedInFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    launchSyncMyEvents()
                    launchSyncMyContactsEvents()
                }
            }
        }
    }

    private fun CoroutineScope.launchSyncMyEvents() = launch {
        syncMyEvents.executeSync(Unit)
    }

    private fun CoroutineScope.launchSyncMyContactsEvents() = launch {
        syncMyContactsEvents.apply {
            invoke(Unit)
            flow.collect()
        }
    }
}
