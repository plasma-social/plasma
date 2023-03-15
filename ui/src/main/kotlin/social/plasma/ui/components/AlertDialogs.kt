package social.plasma.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import social.plasma.ui.R
import social.plasma.ui.theme.PlasmaTheme

@Composable
fun ConfirmationDialog(
    title: String,
    subtitle: String,
    icon: Painter? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String,
) {

    AlertDialog(
        onDismiss = onDismiss,
        icon = icon,
        title = title,
        subtitle = subtitle,
    ) {
        PrimaryButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(confirmLabel)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AlertDialog(
    onDismiss: () -> Unit,
    icon: Painter?,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(subtitle, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                content()
            }
        }
    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewConfirmationDialog() {
    PlasmaTheme {
        ConfirmationDialog(
            title = "Boost this note",
            subtitle = "Boost notes to increase their discoverability across the network",
            icon = painterResource(id = R.drawable.ic_plasma_rocket_outline),
            confirmLabel = "Boost",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
