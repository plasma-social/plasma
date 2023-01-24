package social.plasma

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class PlasmaAndroidApplication : Application(), ImageLoaderFactory {
    private val imageLoaderFactory = PlasmaImageLoaderFactory(this)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun newImageLoader(): ImageLoader = imageLoaderFactory.newImageLoader()
}
