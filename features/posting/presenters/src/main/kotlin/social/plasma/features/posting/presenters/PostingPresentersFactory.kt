package social.plasma.features.posting.presenters

import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import social.plasma.features.posting.screens.ComposingScreen
import javax.inject.Inject


class PostingPresentersFactory @Inject constructor(
    private val composingScreen: ComposingScreenPresenter.Factory,
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