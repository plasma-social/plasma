package social.plasma.shared.utils.api


interface StringManager {
    operator fun get(id: Int): String

    fun getFormattedString(id: Int, args: Map<String, Any>): String
}

