package social.plasma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import social.plasma.ui.theme.PlasmaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlasmaTheme {
                PlasmaApp()
            }
        }
    }
}