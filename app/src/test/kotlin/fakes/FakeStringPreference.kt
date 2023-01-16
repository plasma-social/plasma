package fakes

import social.plasma.prefs.Preference

class FakeStringPreference(
    private var value: String? = null,
) : Preference<String> {
    override fun get(default: String?): String? = value ?: default

    override fun set(value: String) {
        this.value = value
    }

    override fun isSet(): Boolean = value != null

    override fun remove() {
        value = null
    }
}
