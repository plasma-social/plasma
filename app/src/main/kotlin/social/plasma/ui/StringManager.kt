package social.plasma.ui

import androidx.annotation.StringRes

interface StringManager {
    operator fun get(@StringRes id: Int) : String

    fun getFormattedString(@StringRes id: Int, args: Map<String, Any>) : String
}

