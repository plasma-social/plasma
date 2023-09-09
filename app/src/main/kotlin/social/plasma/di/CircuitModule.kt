package social.plasma.di

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.NavDecoration
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
                setDefaultNavDecoration(DefaultDecoration)
            }
            .build()
    }
}


private const val FIVE_PERCENT = 0.05f
private val SlightlyRight = { width: Int -> (width * FIVE_PERCENT).toInt() }
private val SlightlyLeft = { width: Int -> 0 - (width * FIVE_PERCENT).toInt() }

object DefaultDecoration : NavDecoration {
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun <T> DecoratedContent(
        arg: T,
        backStackDepth: Int,
        modifier: Modifier,
        content: @Composable (T) -> Unit,
    ) {
        // Remember the previous stack depth so we know if the navigation is going "back".
        val prevStackDepth = rememberSaveable { mutableIntStateOf(backStackDepth) }
        val diff = backStackDepth - prevStackDepth.value
        prevStackDepth.value = backStackDepth
        AnimatedContent(
            targetState = arg,
            modifier = modifier,
            transitionSpec = {
                // Mirror the forward and backward transitions of activities in Android 33
                if (diff > 0) {
                    slideInHorizontally(tween(), SlightlyRight) + fadeIn() with
                            slideOutHorizontally(
                                tween(),
                                SlightlyLeft
                            ) + fadeOut()
                } else
                    if (diff < 0) {
                        slideInHorizontally(tween(), SlightlyLeft) + fadeIn() with
                                slideOutHorizontally(
                                    tween(),
                                    SlightlyRight
                                ) + fadeOut()
                    } else {
                        // Crossfade if there was no diff
                        fadeIn() with fadeOut()
                    }
                        .using(
                            // Disable clipping since the faded slide-in/out should
                            // be displayed out of bounds.
                            SizeTransform(clip = false)
                        )
            },
            label = "decoratedContent",
        ) {
            content(it)
        }
    }
}
