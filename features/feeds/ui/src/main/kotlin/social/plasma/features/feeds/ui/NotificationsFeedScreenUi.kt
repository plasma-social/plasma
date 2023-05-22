package social.plasma.features.feeds.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiEvent.ChildNav
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiEvent.OnToolbarAvatarTapped
import social.plasma.features.feeds.screens.notifications.NotificationsFeedUiState
import social.plasma.ui.components.AvatarToolBar

class NotificationsFeedScreenUi : Ui<NotificationsFeedUiState> {
    @Composable
    override fun Content(state: NotificationsFeedUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        Scaffold(
            topBar = {
                AvatarToolBar(
                    title = state.title,
                    avatarUrl = state.toolbarAvatar,
                    onAvatarClick = { onEvent(OnToolbarAvatarTapped) })
            }
        ) { paddingValues ->
            CircuitContent(
                FeedScreen(FeedType.Notifications),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onNavEvent = { navEvent -> onEvent(ChildNav(navEvent)) }
            )
        }
    }
}
