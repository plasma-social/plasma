package social.plasma

import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import social.plasma.utils.CrashReportingTree
import social.plasma.workers.ContactListFeedSyncWorker
import social.plasma.workers.DatabasePurgeWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class PlasmaAndroidApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    private val imageLoaderFactory = PlasmaImageLoaderFactory(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            setStrictMode()
        } else {
            installSentry()
            Timber.plant(CrashReportingTree())
        }

        enqueueWorkers()
    }

    private fun enqueueWorkers() {
        val workManager = WorkManager.getInstance(this)
        // TODO move this to a better place
        val refreshContactListRequest =
            PeriodicWorkRequestBuilder<ContactListFeedSyncWorker>(15, TimeUnit.MINUTES)
                .build()

        val purgeDbRequest = PeriodicWorkRequestBuilder<DatabasePurgeWorker>(
            24, TimeUnit.HOURS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "sync-contactlist-feed",
            ExistingPeriodicWorkPolicy.UPDATE,
            refreshContactListRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "purge-db",
            ExistingPeriodicWorkPolicy.UPDATE,
            purgeDbRequest
        )
    }

    private fun setStrictMode() {
        val policy = StrictMode.VmPolicy.Builder()
            .apply {
                detectLeakedSqlLiteObjects()
                detectActivityLeaks()
                detectLeakedRegistrationObjects()
                detectFileUriExposure()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectContentUriWithoutPermission()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    detectCredentialProtectedWhileLocked()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    detectIncorrectContextUse()
                    detectUnsafeIntentLaunch()
                }
                penaltyDeath()
                penaltyLog()
            }.build()

        StrictMode.setVmPolicy(policy)
    }

    private fun installSentry() {
        SentryAndroid.init(this) { options ->
            options.apply {
                dsn =
                    "https://7effa811298d4e0699a82c2b183e3298@o1070658.ingest.sentry.io/4504828647374848"
                isEnableUserInteractionTracing = true
                profilesSampleRate = 0.5
                tracesSampleRate = 0.5
            }
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoaderFactory.newImageLoader()

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
