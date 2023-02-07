package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import social.plasma.ui.base.ViewModelWithNavigatorFactory
import social.plasma.ui.post.PostViewModel

@Module
@InstallIn(ActivityComponent::class)
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelClassKey(PostViewModel::class)
    abstract fun bindsPostViewModelFactory(factory: PostViewModel.Factory): ViewModelWithNavigatorFactory<*>
}