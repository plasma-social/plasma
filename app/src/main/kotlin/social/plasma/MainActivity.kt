package social.plasma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.ui.theme.PlasmaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var circuitConfig: CircuitConfig

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val startScreens: List<Screen> = listOf(HeadlessAuthenticator)

        syncGlobalEvents()
        setContent {
            PlasmaTheme(dynamicStatusBar = true) {
                val backstack =
                    rememberSaveableBackStack { startScreens.forEach { screen -> push(screen) } }
                val circuitNavigator = rememberCircuitNavigator(backstack)
                BackHandler(enabled = backstack.size > 1, onBack = circuitNavigator::pop)
                Surface(color = MaterialTheme.colorScheme.background) {
                    CircuitCompositionLocals(circuitConfig) {
                        ContentWithOverlays {
                            NavigableCircuitContent(circuitNavigator, backstack)
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
}
