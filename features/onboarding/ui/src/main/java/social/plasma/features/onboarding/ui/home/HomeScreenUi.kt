package social.plasma.features.onboarding.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.slack.circuit.CircuitContent
import com.slack.circuit.Ui
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.onboarding.screens.home.HomeUiEvent
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnChildNav
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnFabClick
import social.plasma.features.onboarding.screens.home.HomeUiState
import social.plasma.features.onboarding.screens.login.LoginScreen
import social.plasma.features.onboarding.ui.R
import social.plasma.ui.components.HorizontalSeparator
import social.plasma.ui.components.AvatarToolBar
import social.plasma.ui.R as ComponentsR

class HomeScreenUi : Ui<HomeUiState> {
    @Composable
    override fun Content(state: HomeUiState, modifier: Modifier) {
        val bottomNavItems = remember {
            listOf(
                BottomNavItem(
                    HomeFeeds,
                    ComponentsR.drawable.ic_plasma_feed,
                    R.string.feed
                ),
                BottomNavItem(
                    FeedScreen(feedType = FeedType.Notifications),
                    ComponentsR.drawable.ic_plasma_notifications_outline,
                    R.string.notifications
                ),
            )
        }

        val onEvent = state.onEvent

        var selectedNavItem by rememberSaveable {
            mutableStateOf(bottomNavItems[0])
        }

        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onEvent(OnFabClick) },
                    shape = CircleShape,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )
                }
            },
            topBar = {
                AvatarToolBar(
                    title = stringResource(selectedNavItem.label),
                    avatarUrl = state.avatarUrl,
                    onAvatarClick = { onEvent(HomeUiEvent.OnAvatarClick) },
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    barNavItems = bottomNavItems,
                    selectedItem = selectedNavItem,
                    onSelectItem = {
                        selectedNavItem = it
                    }
                )
            }) { paddingValues ->

            CircuitContent(
                modifier = modifier.padding(paddingValues),
                screen = selectedNavItem.screen,
                onNavEvent = { navEvent -> onEvent(OnChildNav(navEvent)) }
            )
        }
    }
}


@Composable
private fun BottomNavigationBar(
    barNavItems: List<BottomNavItem>,
    selectedItem: BottomNavItem,
    onSelectItem: (BottomNavItem) -> Unit,
) {
    Column {
        HorizontalSeparator()
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            barNavItems.forEach { item ->
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surface,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                    selected = selectedItem == item,
                    onClick = {
                        onSelectItem(item)
                    },
                    label = { Text(stringResource(item.label)) },
                    icon = { Icon(painterResource(item.icon), stringResource(item.label)) },
                )
            }
        }
    }
}