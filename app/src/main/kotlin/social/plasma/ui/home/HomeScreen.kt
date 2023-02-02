package social.plasma.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import social.plasma.PubKey
import social.plasma.R
import social.plasma.nostr.models.UserMetaData
import social.plasma.ui.components.PlasmaTab
import social.plasma.ui.components.PlasmaTabRow
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.ContactsFeed
import social.plasma.ui.feed.GlobalFeed
import social.plasma.ui.feed.RepliesFeed
import social.plasma.ui.theme.PlasmaTheme


@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    navigateToThread: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeScreenUiState.Loaded -> HomeScreen(
            onNavigateToProfile = onNavigateToProfile,
            modifier = modifier,
            userMetaData = state.userMetadata,
            userPubKey = state.userPubkey,
            navigateToThread = navigateToThread,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
    navigateToThread: (String) -> Unit,
    userMetaData: UserMetaData,
    userPubKey: PubKey,
) {
    val tabs = remember { HomeTab.values() }

    val navController = rememberNavController()
    val currentStackEntry by navController.currentBackStackEntryAsState()

    val selectedTab = rememberSaveable(currentStackEntry) { currentStackEntry.getCurrentTab() }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            ElevatedCard(
                shape = RectangleShape,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 3.dp,
                )
            ) {
                Column {
                    RootScreenToolbar(
                        title = stringResource(R.string.feed),
                        avatarUrl = userMetaData.picture ?: "",
                        onAvatarClick = { onNavigateToProfile(userPubKey) },
                    )
                    PlasmaTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = selectedTab == tab

                            PlasmaTab(
                                selected = isSelected,
                                title = tab.title,
                                icon = tab.icon,
                            ) {
                                if (!isSelected) {
                                    navController.popBackStack()
                                    navController.navigate(tab.name) {
                                        launchSingleTop = true

                                        restoreState = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HomeTab.Following.name
        ) {
            // TODO figure out a way to combine all of these
            composable(HomeTab.Following.name) {
                ContactsFeed(
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues),
                    navigateToThread = navigateToThread,
                )
            }

            composable(HomeTab.Global.name) {
                GlobalFeed(
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues),
                    navigateToThread = navigateToThread,

                    )
            }

            composable(HomeTab.Replies.name) {
                RepliesFeed(
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues),
                    navigateToThread = navigateToThread,
                )
            }

        }
    }
}

private fun NavBackStackEntry?.getCurrentTab(): HomeTab {
    for (tab in HomeTab.values()) {
        if (tab.name == this?.destination?.route)
            return tab
    }

    return HomeTab.Following
}


enum class HomeTab(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    Following(
        title = R.string.following,
        icon = R.drawable.ic_plasma_follow,
    ),
    Replies(
        title = R.string.replies,
        icon = R.drawable.ic_plasma_replies,
    ),
    Global(
        title = R.string.global,
        icon = R.drawable.ic_plasma_global_outline,
    ),
    ;
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    PlasmaTheme {
        HomeScreen(onNavigateToProfile = {}, navigateToThread = {})
    }
}
