package social.plasma.ui.navigation

interface Navigator {
    fun goToRoute(route: String)

    fun goBack()
}