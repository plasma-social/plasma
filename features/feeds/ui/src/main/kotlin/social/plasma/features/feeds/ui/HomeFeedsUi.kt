package social.plasma.features.feeds.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.slack.circuit.CircuitContent
import com.slack.circuit.Screen
import com.slack.circuit.Ui
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiEvent
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiEvent.ChildNav
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiState
import social.plasma.ui.components.AvatarToolBar
import social.plasma.ui.components.HorizontalSeparator
import social.plasma.ui.components.PlasmaTab
import social.plasma.ui.components.PlasmaTabRow

@OptIn(ExperimentalFoundationApi::class)
class HomeFeedsUi : Ui<HomeFeedsUiState> {
    @Composable
    override fun Content(state: HomeFeedsUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        val tabs = remember {
            listOf(
                ScreenTab(
                    screen = FeedScreen(feedType = FeedType.Following),
                    title = R.string.following,
                    icon = social.plasma.ui.R.drawable.ic_plasma_follow
                ),
                ScreenTab(
                    screen = FeedScreen(feedType = FeedType.Replies),
                    title = R.string.replies,
                    icon = social.plasma.ui.R.drawable.ic_plasma_replies
                ),
            )
        }

        val pagerState = rememberPagerState()

        val selectedTab = pagerState.currentPage
        Column(
            modifier = modifier,
        ) {
            AvatarToolBar(
                title = state.title,
                avatarUrl = state.toolbarAvatar,
                onAvatarClick = { onEvent(HomeFeedsUiEvent.OnToolbarAvatarTapped) },
            )
            PlasmaTabRow(
                selectedTabIndex = selectedTab,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val coroutineScope = rememberCoroutineScope()
                    PlasmaTab(selected = isSelected,
                        title = tab.title,
                        icon = tab.icon,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } })
                }
            }
            HorizontalSeparator()

            HorizontalPager(pageCount = tabs.size, state = pagerState) {
                CircuitContent(
                    screen = tabs[it].screen,
                    onNavEvent = { navEvent -> onEvent(ChildNav(navEvent)) },
                )
            }
        }
    }
}

data class ScreenTab(val screen: Screen, @StringRes val title: Int, @DrawableRes val icon: Int)
