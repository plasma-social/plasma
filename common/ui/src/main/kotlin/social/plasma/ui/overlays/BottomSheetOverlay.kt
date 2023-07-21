package social.plasma.ui.overlays

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.launch
import social.plasma.ui.rememberStableCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetOverlay<Model : Any, Result : Any>(
    private val model: Model,
    private val onDismiss: () -> Result,
    private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        val sheetState = rememberModalBottomSheetState()

        val coroutineScope = rememberStableCoroutineScope()
        BackHandler(enabled = sheetState.isVisible) {
            coroutineScope
                .launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        navigator.finish(onDismiss())
                    }
                }
        }

        ModalBottomSheet(
            windowInsets = WindowInsets.navigationBars,
            content = {
                content(model) { result ->
                    coroutineScope.launch {
                        try {
                            sheetState.hide()
                        } finally {
                            navigator.finish(result)
                        }
                    }
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            onDismissRequest = { navigator.finish(onDismiss()) },
        )

        LaunchedEffect(Unit) { sheetState.show() }
    }
}
