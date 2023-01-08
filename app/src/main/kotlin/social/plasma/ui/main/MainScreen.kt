package social.plasma.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Segment
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import social.plasma.R
import social.plasma.ui.feed.Feed
import social.plasma.ui.navigation.Screen
import social.plasma.ui.navigation.isActiveScreen
import social.plasma.ui.theme.PlasmaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
) {
    val bottomNavItems = listOf(
        NavigationBarScreen(Screen.Home, Icons.Outlined.Segment),
        NavigationBarScreen(Screen.Search, Icons.Outlined.Search),
        NavigationBarScreen(Screen.Messages, Icons.Outlined.Forum),
        NavigationBarScreen(Screen.Notifications, Icons.Outlined.Notifications),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navHostController.currentBackStackEntryAsState()

                bottomNavItems.forEach { (screen, icon) ->
                    NavigationBarItem(
                        selected = navBackStackEntry?.isActiveScreen(screen) ?: false,
                        onClick = { navHostController.navigate(screen.route) },
                        icon = { Icon(icon, stringResource(screen.name)) },
                    )
                }
            }
        }) { paddingValues ->
        NavHost(
            navController = navHostController,
            modifier = Modifier.padding(paddingValues),
            startDestination = Screen.Home.route
        ) {
            bottomNavItems.forEach { (screen) ->
                composable(screen.route) {
                    Feed(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}


data class NavigationBarScreen(val screen: Screen, val icon: ImageVector)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
    PlasmaTheme {
        MainScreen()
    }
}