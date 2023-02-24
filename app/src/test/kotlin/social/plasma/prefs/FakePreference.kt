package social.plasma.prefs

data class FakePreference<T>(
    var value: T? = null,
) : Preference<T> {
    override fun get(default: T?): T? {
        return value
    }

    override fun set(value: T) {
        this.value = value
    }

    override fun isSet(): Boolean {
        return value != null
    }

    override fun remove() {
        value = null
    }

}
