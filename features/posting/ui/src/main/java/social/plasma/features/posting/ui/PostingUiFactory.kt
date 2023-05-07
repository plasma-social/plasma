package social.plasma.features.posting.ui

import com.slack.circuit.CircuitContext
import com.slack.circuit.Screen
import com.slack.circuit.Ui
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import social.plasma.features.posting.ui.composepost.CreatePostScreenUi
import social.plasma.features.posting.screens.ComposingScreen
import javax.inject.Inject

class PostingUiFactory @Inject constructor(): Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when(screen) {
            is ComposingScreen -> CreatePostScreenUi()
            else -> null
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class PostingModule {
    @Binds
    @IntoSet
    abstract fun bindsUiFactory(impl: PostingUiFactory) : Ui.Factory
}
