package social.plasma

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlasmaAndroidApplication : Application(), ImageLoaderFactory {
    private val imageLoaderFactory = PlasmaImageLoaderFactory(this)
    override fun newImageLoader(): ImageLoader = imageLoaderFactory.newImageLoader()
}
