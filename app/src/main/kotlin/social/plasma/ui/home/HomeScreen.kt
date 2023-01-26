package social.plasma.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.PlasmaTab
import social.plasma.ui.components.PlasmaTabRow
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.ContactsFeed
import social.plasma.ui.feed.GlobalFeed
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
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
                    RootScreenToolbar()
                    PlasmaTabRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        selectedTabIndex = selectedTab.ordinal,
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isFirstTab = index == 0
                            val isLastTab = index == tabs.lastIndex
                            val isSelected = selectedTab == tab

                            PlasmaTab(
                                useLeftRoundShape = isFirstTab,
                                useRightRoundShape = isLastTab,
                                selected = isSelected,
                                title = tab.title,
                                icon = tab.icon,
                                onClick = {
                                    if (tab != selectedTab) {
                                        navController.popBackStack()
                                        navController.navigate(tab.name) {
                                            launchSingleTop = true

                                            restoreState = true
                                        }
                                    }
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

        }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HomeTab.Following.name
        ) {
            composable(HomeTab.Following.name) {
                ContactsFeed(
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            composable(HomeTab.Global.name) {
                GlobalFeed(
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues)
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
        icon = R.drawable.ic_plasma_follow
    ),
    Global(
        title = R.string.global,
        icon = R.drawable.ic_plasma_global_outline
    ),
    ;
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    PlasmaTheme {
        HomeScreen(onNavigateToProfile = {})
    }
}
