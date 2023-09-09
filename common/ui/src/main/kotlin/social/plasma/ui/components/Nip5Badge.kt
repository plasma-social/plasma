package social.plasma.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import social.plasma.models.Nip5Status
import social.plasma.ui.R

@Composable
fun Nip5Badge(
    nip5Status: Nip5Status,
    modifier: Modifier = Modifier,
) {
    when (nip5Status) {
        is Nip5Status.Missing -> {} // no badge
        is Nip5Status.Set -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedContent(nip5Status, label = "nip5status") { status ->
                    when (status) {
                        is Nip5Status.Set.Invalid -> {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Default.ErrorOutline,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = null,
                            )
                        }

                        is Nip5Status.Set.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp),
                                strokeWidth = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        is Nip5Status.Set.Valid -> {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.drawable.ic_plasma_verified),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    nip5Status.identifier,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
