package social.plasma.features.search.presenters

import com.slack.circuit.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class SearchPresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsSearchPresenterFactory(factory: SearchPresentersFactory): Presenter.Factory
}

