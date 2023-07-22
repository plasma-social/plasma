package social.plasma.ui.components

import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.OverlayNavigator

@Immutable
data class SelectZapAmountModel(
    val amountBuckets: List<Long>,
    val initialAmount: Long = amountBuckets.firstOrNull() ?: 0L,
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun SelectZapAmountComponent(
    modifier: Modifier = Modifier,
    model: SelectZapAmountModel,
    overlayNav: OverlayNavigator<Long>,
) {
    var enteredAmount by remember {
        mutableStateOf(
            TextFieldValue(model.initialAmount.toString())
        )
    }

    val numberFormatter = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Zap Amount",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            model.amountBuckets.forEach { bucket ->
                if ((enteredAmount.text.toLongOrNull() ?: 0L) == bucket) {
                    PrimaryButton(onClick = {
                        enteredAmount = TextFieldValue("")
                    }) {
                        Text(numberFormatter.format(bucket))
                    }
                } else {
                    OutlinedPrimaryButton(onClick = {
                        enteredAmount = TextFieldValue(
                            bucket.toString(),
                            selection = TextRange(bucket.toString().length)
                        )
                    }) {
                        Text(numberFormatter.format(bucket))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            label = { Text("Custom Amount") },
            value = enteredAmount,
            onValueChange = { enteredAmount = it },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    overlayNav.finish(enteredAmount.text.toLongOrNull() ?: 0L)
                }
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = withHapticFeedBack {
                overlayNav.finish(enteredAmount.text.toLongOrNull() ?: 0L)
            },
            enabled = (enteredAmount.text.toLongOrNull() ?: 0) != 0L,
        ) {
            Text(text = "Zap")
        }
    }
}
