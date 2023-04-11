package social.plasma.ui.components

import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlasmaTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        tabs = tabs,
        divider = {},
    )
}
