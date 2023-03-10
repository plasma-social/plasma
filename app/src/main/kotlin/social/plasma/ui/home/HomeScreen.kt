package social.plasma.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import social.plasma.R
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.nostr.models.UserMetaData
import social.plasma.ui.components.PlasmaTab
import social.plasma.ui.components.PlasmaTabRow
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.ContactsFeed
import social.plasma.ui.feed.FollowingFeedViewModel
import social.plasma.ui.feed.RepliesFeed
import social.plasma.ui.feed.RepliesFeedViewModel
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.R as UiR


@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToThread: (NoteId) -> Unit,
    navigateToPost: () -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeScreenUiState.Loaded -> HomeScreen(
            onNavigateToProfile = onNavigateToProfile,
            modifier = modifier,
            navigateToThread = onNavigateToThread,
            navigateToPost = navigateToPost,
            userMetaData = state.userMetadata,
            userPubKey = state.userPubkey,
            onNavigateToReply = onNavigateToReply,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
    navigateToThread: (NoteId) -> Unit,
    navigateToPost: () -> Unit,
    userMetaData: UserMetaData,
    userPubKey: PubKey,
    onNavigateToReply: (NoteId) -> Unit,
    followingFeedViewModel: FollowingFeedViewModel = hiltViewModel(),
    repliesFeedViewModel: RepliesFeedViewModel = hiltViewModel(),
) {
    val tabs = remember { HomeTab.values().asList() }

    val pagerState = rememberPagerState()

    val repliesListState = rememberLazyListState()
    val followingListState = rememberLazyListState()

    val selectedTab = pagerState.currentPage

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            val coroutineScope = rememberCoroutineScope()

            HomeScreenTopBar(
                tabs = tabs,
                userMetaData = userMetaData,
                onNavigateToProfile = { onNavigateToProfile(userPubKey) },
                selectedTabIndex = selectedTab,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }) { paddingValues ->

        HorizontalPager(count = tabs.size, state = pagerState) {
            when (tabs[it]) {
                HomeTab.Following -> ContactsFeed(
                    modifier = Modifier.padding(paddingValues),
                    onNavigateToProfile = onNavigateToProfile,
                    navigateToThread = navigateToThread,
                    onAddNote = navigateToPost,
                    onNavigateToReply = onNavigateToReply,
                    state = followingListState,
                    viewModel = followingFeedViewModel,
                )

                HomeTab.Replies -> RepliesFeed(
                    modifier = Modifier.padding(paddingValues),
                    onNavigateToProfile = onNavigateToProfile,
                    navigateToThread = navigateToThread,
                    onAddNote = navigateToPost,
                    onNavigateToReply = onNavigateToReply,
                    state = repliesListState,
                    viewModel = repliesFeedViewModel,
                )
            }
        }
    }
}

@Composable
fun HomeScreenTopBar(
    userMetaData: UserMetaData,
    onNavigateToProfile: () -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<HomeTab>,
) {
    Column {
        RootScreenToolbar(
            title = stringResource(R.string.feed),
            avatarUrl = userMetaData.picture ?: "",
            onAvatarClick = onNavigateToProfile,
        )
        PlasmaTabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index

                PlasmaTab(
                    selected = isSelected,
                    title = tab.title,
                    icon = tab.icon,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

enum class HomeTab(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    Following(
        title = UiR.string.following,
        icon = UiR.drawable.ic_plasma_follow,
    ),
    Replies(
        title = R.string.replies,
        icon = UiR.drawable.ic_plasma_replies,
    ),
//    Global(
//        title = R.string.global,
//        icon = R.drawable.ic_plasma_global_outline,
//    ),
    ;
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    PlasmaTheme {
        HomeScreen(
            onNavigateToProfile = {},
            onNavigateToThread = {},
            navigateToPost = {},
            onNavigateToReply = {},
        )
    }
}
