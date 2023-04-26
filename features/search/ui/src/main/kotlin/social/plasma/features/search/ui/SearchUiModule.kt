package social.plasma.features.search.ui

import com.slack.circuit.Ui
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class SearchUiModule {
    @Binds
    @IntoSet
    abstract fun bindsUiFactory(impl: SearchUiFactory): Ui.Factory
}
