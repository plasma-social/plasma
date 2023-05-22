package social.plasma.profile.presenters

import com.slack.circuit.runtime.presenter.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ProfilePresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsProfilePresenterFactory(factory: ProfilePresentersFactory): Presenter.Factory
}

