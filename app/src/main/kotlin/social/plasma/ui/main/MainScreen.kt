package social.plasma.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import social.plasma.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.feed.Feed
import social.plasma.ui.home.Home
import social.plasma.ui.navigation.Screen
import social.plasma.ui.navigation.isActiveTab
import social.plasma.ui.profile.Profile
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navBackStackEntry by navHostController.currentBackStackEntryAsState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Avatar(
                            imageUrl = "https://api.dicebear.com/5.x/bottts/jpg",
                            contentDescription = ""
                        )
                    }
                },
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (screen, icon) ->
                    NavigationBarItem(
                        selected = navBackStackEntry?.isActiveTab(screen) ?: false,
                        onClick = {
                            navHostController.navigate(screen.route)
                        },
                        icon = { Icon(painterResource(icon), stringResource(screen.name)) },
                    )
                }
            }
        }) { paddingValues ->
        // TODO move to its own file
        NavHost(
            navController = navHostController,
            modifier = Modifier.padding(paddingValues),
            startDestination = Screen.Home.route
        ) {
            bottomNavItems.forEach { (screen) ->
                composable(screen.route) {
                    when (screen) {
                        is Screen.Home -> Home()
                        is Screen.Global -> Profile()
                        else -> Feed()
                    }
                }
            }
        }
    }
}

private val bottomNavItems = listOf(
    NavigationBarTab(Screen.Home, R.drawable.ic_plasma_feed),
    NavigationBarTab(Screen.Global, R.drawable.ic_plasma_global_outline),
    NavigationBarTab(Screen.Notifications, R.drawable.ic_plasma_notifications_outline),
)

private data class NavigationBarTab(val screen: Screen, @DrawableRes val icon: Int)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMainScreen() {
    PlasmaTheme {
        MainScreen()
    }
}