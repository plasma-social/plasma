package social.plasma

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.SyncContactsEvents
import social.plasma.domain.interactors.SyncMyEvents
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject

class SyncManager @Inject constructor(
    accountStateRepository: AccountStateRepository,
    private val syncMyEvents: SyncMyEvents,
    private val syncMyContactsEvents: SyncContactsEvents,
) {

    private val isLoggedInFlow = accountStateRepository.isLoggedIn
    private var job: Job? = null

    suspend fun startSync() {
        job?.cancel()
        job = coroutineScope {
            launch {
                isLoggedInFlow.collect { isLoggedIn ->
                    if (isLoggedIn) {
                        launchSyncMyEvents()
                        launchSyncMyContactsEvents()
                    }
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
