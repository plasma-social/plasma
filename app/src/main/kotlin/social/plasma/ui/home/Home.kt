package social.plasma.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
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
import social.plasma.ui.feed.Feed

@Composable
fun Home(
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Posts) }
    val tabs = remember { HomeTab.values() }

    Column(
        modifier = modifier,
    ) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
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
        when (selectedTab) {
            HomeTab.Posts -> Feed()
            HomeTab.Replies -> Feed()
        }
    }
}

enum class HomeTab(@StringRes val title: Int) {
    Posts(R.string.posts),
    Replies(R.string.posts_and_replies)
}