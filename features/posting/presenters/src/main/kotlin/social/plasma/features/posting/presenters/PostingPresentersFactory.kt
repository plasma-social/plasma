package social.plasma.features.posting.presenters

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import social.plasma.features.posting.screens.ComposingScreen
import javax.inject.Inject


class PostingPresentersFactory @Inject constructor(
    private val composingScreen: CreatePostScreenPresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ComposingScreen -> composingScreen.create(screen, navigator)
            else -> null
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class PostingPresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsPresentersFactory(impl: PostingPresentersFactory): Presenter.Factory
}
