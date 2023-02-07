package social.plasma.ui.base

import androidx.lifecycle.ViewModel
import social.plasma.ui.navigation.Navigator

interface ViewModelWithNavigatorFactory<out VM : ViewModel> {

    fun create(navigator: Navigator): VM
}