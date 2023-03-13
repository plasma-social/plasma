package social.plasma

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import social.plasma.sync.ContactListFeedSyncWorker
import social.plasma.utils.CrashReportingTree
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
        } else {
            Timber.plant(CrashReportingTree())
        }

        val workManager = WorkManager.getInstance(this)
        // TODO move this to a better place
        val refreshContactListRequest =
            PeriodicWorkRequestBuilder<ContactListFeedSyncWorker>(15, TimeUnit.MINUTES)
                .build()
        workManager.enqueueUniquePeriodicWork(
            "sync-contactlist-feed",
            ExistingPeriodicWorkPolicy.UPDATE,
            refreshContactListRequest
        )

    }

    override fun newImageLoader(): ImageLoader = imageLoaderFactory.newImageLoader()

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
