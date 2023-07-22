package social.plasma.ui.overlays

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.OverlayHost
import social.plasma.ui.components.SelectZapAmountComponent
import social.plasma.ui.components.SelectZapAmountModel

private val defaultModel = SelectZapAmountModel(
    amountBuckets = listOf(
        21,
        42,
        69,
        420,
        1_000,
        5_000,
        10_000
    )
)

suspend fun OverlayHost.getZapAmount(
    model: SelectZapAmountModel = defaultModel,
): Long {
    return show(
        BottomSheetOverlay(
            model = model,
            onDismiss = { 0L },
        ) { overlayModel, overlayNav ->
            SelectZapAmountComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                model = overlayModel,
                overlayNav = overlayNav
            )
        }
    )
}
