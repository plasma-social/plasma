package social.plasma.features.feeds.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.flow.flowOf
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.screens.hashtags.ButtonUiState
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent.OnFollowButtonClick
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiState
import social.plasma.ui.R
import social.plasma.ui.components.OutlinedPrimaryButton
import social.plasma.ui.components.PrimaryButton
import social.plasma.ui.components.withHapticFeedBack
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
class HashTagFeedScreenUi : Ui<HashTagScreenUiState> {
    @Composable
    override fun Content(state: HashTagScreenUiState, modifier: Modifier) {
        val onScreenEvent = state.onEvent

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { onScreenEvent(HashTagScreenUiEvent.OnNavigateBack) }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_back),
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        FollowButton(state, onScreenEvent)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
        ) { paddingValues ->
            FeedUiContent(
                state = state.feedState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 8.dp),
            )
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun FollowButton(
        state: HashTagScreenUiState,
        onScreenEvent: (HashTagScreenUiEvent) -> Unit,
    ) {
        AnimatedContent(
            targetState = state.followButtonUiState,
            label = "follow-hashtag-button"
        ) { buttonState ->
            when (buttonState.style) {
                ButtonUiState.Style.Primary -> {
                    PrimaryButton(
                        onClick = withHapticFeedBack { onScreenEvent(OnFollowButtonClick) },
                        enabled = buttonState.enabled
                    ) {
                        Text(buttonState.label)
                    }
                }

                ButtonUiState.Style.PrimaryOutline -> {
                    OutlinedPrimaryButton(
                        onClick = withHapticFeedBack { onScreenEvent(OnFollowButtonClick) },
                        enabled = buttonState.enabled,
                    ) {
                        Text(buttonState.label)
                    }
                }
            }

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewHashtagFeedScreenUi() {
    PlasmaTheme {
        val feedState = FeedUiState(
            listState = rememberLazyListState(),
            pagingFlow = flowOf(PagingData.empty()),
            getOpenGraphMetadata = { null },
            onEvent = {},
        )
        HashTagFeedScreenUi().Content(
            state = HashTagScreenUiState(
                title = "#foodstr",
                feedState = feedState,
                followButtonUiState = ButtonUiState(label = "Follow"),
                onEvent = {},
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
