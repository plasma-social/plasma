package social.plasma.ui.post

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
fun postViewModel(navigator: Navigator): PostViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryEntryPoint::class.java,
    ).postViewModelFactory()

    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(navigator) as T
            }
        },
    )
}