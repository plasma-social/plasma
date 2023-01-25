package social.plasma.ui.components

import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PlasmaTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    TabRow(
        modifier = modifier,
        containerColor = Color.Transparent,
        selectedTabIndex = selectedTabIndex,
        indicator = {},
        divider = {},
        tabs = tabs,
    )
}
