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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.PlasmaTab
import social.plasma.ui.components.PlasmaTabRow
import social.plasma.ui.components.RootScreenToolbar
import social.plasma.ui.feed.Feed
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Following) }
    val tabs = remember { HomeTab.values() }

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
                                    selectedTab = tab
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

        }) { paddingValues ->
        when (selectedTab) {
            HomeTab.Following -> Feed(
                modifier = Modifier.padding(paddingValues),
                onNavigateToProfile = onNavigateToProfile
            )

            HomeTab.Global -> Feed(
                modifier = Modifier.padding(paddingValues),
                onNavigateToProfile = onNavigateToProfile
            )
        }
    }
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
