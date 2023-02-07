package social.plasma.ui.base

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.EntryPointAccessors
import social.plasma.di.ViewModelFactoryEntryPoint
import social.plasma.ui.navigation.Navigator

@Composable
inline fun <reified VM : ViewModel> viewModelWithNavigator(navigator: Navigator): VM {
    val map = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryEntryPoint::class.java,
    ).navigatorViewModelFactoryMap()

    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val factory = requireNotNull(map[VM::class.java]) {
                    "ViewModelWithNavigatorFactory not found for class ${VM::class.java}"
                }
                return factory.create(navigator) as T
            }
        },
    )
}