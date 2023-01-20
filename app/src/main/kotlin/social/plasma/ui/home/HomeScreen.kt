package social.plasma.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import social.plasma.R
import social.plasma.models.PubKey
import social.plasma.ui.feed.Feed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: (PubKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Following) }
    val tabs = remember { HomeTab.values() }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TabRow(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                selectedTabIndex = selectedTab.ordinal) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = stringResource(tab.title),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    )
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

enum class HomeTab(@StringRes val title: Int) {
    Following(R.string.following),
    Global(R.string.global)
}
