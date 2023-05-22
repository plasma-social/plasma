package social.plasma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import com.slack.circuit.runtime.Screen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.ui.theme.PlasmaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var circuitConfig: CircuitConfig

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    @ActivityRetainedScoped
    lateinit var coroutineScope: CoroutineScope

    private val newScreenRequest = mutableStateOf<Screen?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val startScreens: List<Screen> =
            listOf(HeadlessAuthenticator(exitScreen = getStartingScreen(intent)))

        setContent {
            PlasmaTheme(dynamicStatusBar = true) {
                val backstack =
                    rememberSaveableBackStack { startScreens.forEach { screen -> push(screen) } }
                val circuitNavigator = rememberCircuitNavigator(backstack)

                LaunchedEffect(newScreenRequest.value, circuitNavigator) {
                    val newScreen = newScreenRequest.value
                    if (newScreen != null) {
                        circuitNavigator.goTo(newScreen)
                        newScreenRequest.value = null
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    CompositionLocalProvider(LocalRetainedStateRegistry provides continuityRetainedStateRegistry()) {
                        CircuitCompositionLocals(circuitConfig) {
                            ContentWithOverlays {
                                NavigableCircuitContent(circuitNavigator, backstack)
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val screen = getStartingScreen(intent)
        newScreenRequest.value = screen
    }

    private fun getStartingScreen(intent: Intent?): Screen? {
        return when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    ComposingScreen(content = text)
                } else {
                    null
                }
            }

            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isChangingConfigurations.not()) {
            coroutineScope.cancel()
        }
    }
}
