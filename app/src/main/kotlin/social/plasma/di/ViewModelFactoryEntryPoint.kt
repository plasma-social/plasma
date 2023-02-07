package social.plasma.di

import androidx.lifecycle.ViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import social.plasma.ui.base.ViewModelWithNavigatorFactory

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryEntryPoint {

    fun navigatorViewModelFactoryMap(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModelWithNavigatorFactory<*>>
}