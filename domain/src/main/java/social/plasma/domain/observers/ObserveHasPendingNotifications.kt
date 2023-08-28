package social.plasma.domain.observers

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import okio.ByteString.Companion.toByteString
import social.plasma.domain.SubjectInteractor
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class ObserveHasPendingNotifications @Inject constructor(
    private val accountStateRepository: AccountStateRepository,
    private val notesDao: NoteRepository,
) : SubjectInteractor<Unit, Boolean>() {

    override fun createObservable(params: Unit): Flow<Boolean> = flow {
        val pubkey = PubKey(accountStateRepository.getPublicKey()!!.toByteString())

        emitAll(combine(
            accountStateRepository.observeLastNotificationSeenTimestamp(),
            notesDao.observeMostRecentNotification(pubkey)
        ) { lastNotificationSeenTimestamp, mostRecentNotification ->
            val lastNotificationTimestamp =
                Instant.ofEpochSecond(mostRecentNotification?.createdAt ?: 0L)

            Timber.d("lastNotificationSeenTimestamp: $lastNotificationSeenTimestamp")
            Timber.d("lastNotificationTimestamp: $lastNotificationTimestamp")

            lastNotificationTimestamp > lastNotificationSeenTimestamp
        })
    }
}
