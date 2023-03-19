package social.plasma.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import timber.log.Timber

@HiltWorker
class ContactListFeedSyncWorker @AssistedInject constructor(
    private val legacyNoteRepository: NoteRepository,
    private val accountStateRepository: AccountStateRepository,
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // check if user is logged in first
        if (!accountStateRepository.isLoggedIn.first()) return Result.success()

        val newNotes = legacyNoteRepository.refreshContactsNotes()
        Timber.d("New notes count: ${newNotes.size}")
        return Result.success()
    }
}