package social.plasma.features.feeds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import com.slack.circuit.foundation.CircuitContent
import kotlinx.coroutines.delay
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiEvent
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiEvent.OnFeedCountChange
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventFeedUi(
    modifier: Modifier,
    state: EventFeedUiState,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    headerContent: LazyListScope.() -> Unit = {},
    itemContainer: @Composable LazyListScope.(@Composable () -> Unit) -> Unit = { it() },
) {
    val onEvent = state.onEvent

    val pagingItems = state.items.collectAsLazyPagingItems()

    val showLoading by produceState(initialValue = false, pagingItems.itemCount) {
        delay(500)
        value = pagingItems.itemCount == 0
    }

    LaunchedEffect(pagingItems.itemCount) {
        onEvent(OnFeedCountChange(pagingItems.itemCount))
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            state = state.listState,
            verticalArrangement = verticalArrangement,
        ) {
            headerContent()

            items(
                pagingItems.itemCount,
                contentType = pagingItems.itemContentType { it.kind },
                key = pagingItems.itemKey { it.id },
            ) { index ->
                val event = pagingItems[index]
                this@LazyColumn.itemContainer {
                    if (event == null) {
                        LoadingCard(
                            modifier = Modifier.animateItemPlacement()
                        )
                    } else {
                        val itemScreen = remember(event) { state.screenProvider(event) }

                        CircuitContent(
                            modifier = Modifier.animateItemPlacement(),
                            screen = itemScreen,
                            onNavEvent = { onEvent(EventFeedUiEvent.OnChildNavEvent(it)) },
                        )
                    }
                }
            }
        }

        if (state.displayRefreshButton) {
            RefreshButton(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                text = state.refreshText,
                onClick = { onEvent(EventFeedUiEvent.OnRefreshButtonClick) }
            )
        }

        if (showLoading) {
            LinearProgressIndicator(
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun RefreshButton(modifier: Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        onClick = onClick
    ) {
        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
        Text(text = text)
    }
}

@Composable
internal fun LoadingCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .placeholder(
                true,
                color = MaterialTheme.colorScheme.surface,
                highlight = PlaceholderHighlight.shimmer(MaterialTheme.colorScheme.background),
                shape = CardDefaults.shape,
            ),
    ) {}
}
