package social.plasma.prefs

interface Preference<T> {
    fun get(default: T): T?

    fun set(value: T)

    fun isSet(): Boolean

    fun remove()
}
