package social.plasma.di

import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class CircuitModule {
    @Provides
    @ActivityRetainedScoped
    fun provideCircuit(
        presenterFactories: @JvmSuppressWildcards Set<Presenter.Factory>,
        uiFactories: @JvmSuppressWildcards Set<Ui.Factory>,
    ): CircuitConfig {
        return CircuitConfig.Builder()
            .apply {
                for (factory in presenterFactories) {
                    addPresenterFactory(factory)
                }
                for (factory in uiFactories) {
                    addUiFactory(factory)
                }
            }
            .build()
    }
}
