package social.plasma.ui.navigation

import androidx.annotation.StringRes
import social.plasma.models.PubKey
import social.plasma.R

sealed class Screen(
    val route: String,
    @StringRes val name: Int,
) {
    object Thread : Screen(route = "thread/{noteId}", name = R.string.thread) {
        fun buildRoute(noteId: String): String = route.replace("{noteId}", noteId)
    }

    object Profile : Screen(route = "profile/{pubkey}", name = R.string.profile) {
        fun buildRoute(pubKey: PubKey): String = route.replace("{pubkey}", pubKey.hex)
    }

    object Reply : Screen(route = "reply/{noteId}", name = R.string.reply) {
        fun buildRoute(noteId: String): String = route.replace("{noteId}", noteId)
    }

    object Home : Screen(route = "home", name = R.string.home)

    object Messages : Screen(route = "messages", name = R.string.messages)

    object Notifications : Screen(
        route = "notifications",
        name = R.string.notifications,
    )

    object PostNote : Screen(
        route = "post-note",
        name = R.string.post_note,
    )
}
