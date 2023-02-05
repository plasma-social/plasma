package social.plasma.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import social.plasma.ui.post.PostViewModel

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryEntryPoint {
    fun postViewModelFactory(): PostViewModel.Factory
}