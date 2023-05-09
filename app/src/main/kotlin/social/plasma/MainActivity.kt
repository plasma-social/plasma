package social.plasma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.slack.circuit.CircuitCompositionLocals
import com.slack.circuit.CircuitConfig
import com.slack.circuit.NavigableCircuitContent
import com.slack.circuit.Screen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.push
import com.slack.circuit.rememberCircuitNavigator
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    private val newScreenRequest = mutableStateOf<Screen?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val startScreens: List<Screen> =
            listOf(HeadlessAuthenticator(exitScreen = getStartingScreen(intent)))

        syncGlobalEvents()
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

                BackHandler(enabled = backstack.size > 1, onBack = circuitNavigator::pop)
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

    private fun syncGlobalEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(CREATED) {
                syncManager.startSync()
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
}
