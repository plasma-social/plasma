package social.plasma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.cash.nostrino.crypto.PubKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.sentry.Sentry
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.EventsDao
import social.plasma.data.daos.LastRequestDao
import social.plasma.models.Request
import social.plasma.shared.repositories.api.AccountStateRepository

@HiltWorker
class DatabasePurgeWorker @AssistedInject constructor(
    private val eventsDao: EventsDao,
    private val lastRequestDao: LastRequestDao,
    private val accountStateRepository: AccountStateRepository,
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val transaction = Sentry.startTransaction("DatabasePurgeWorker", "start")
        return try {
            // check if user is logged in first
            if (!accountStateRepository.isLoggedIn.first()) return Result.success()

            val pubkey = PubKey(accountStateRepository.getPublicKey()!!.toByteString())

            // Purge events keeping any events created by or referencing the current user, and an additional 1000 events.
            eventsDao.purgeEvents(excludedPubkey = pubkey.hex(), keepCount = 1000)
            lastRequestDao.purgeRequests(listOf(Request.SYNC_HASHTAG, Request.SYNC_THREAD))
            Result.success()
        } catch (e: Exception) {
            transaction.throwable = e
            Result.failure()
        } finally {
            transaction.finish()
        }
    }
}
