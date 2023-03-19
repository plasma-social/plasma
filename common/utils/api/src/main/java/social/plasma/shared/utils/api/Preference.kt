package social.plasma.shared.utils.api

interface Preference<T> {
    fun get(default: T?): T?

    fun set(value: T)

    fun isSet(): Boolean

    fun remove()
}
