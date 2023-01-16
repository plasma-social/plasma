package fakes

import social.plasma.prefs.Preference

class FakeByteArrayPreference(
    private var value: ByteArray? = null,
) : Preference<ByteArray> {
    override fun get(default: ByteArray?): ByteArray? = value ?: default

    override fun set(value: ByteArray) {
        this.value = value
    }

    override fun isSet(): Boolean = value != null

    override fun remove() {
        value = null
    }
}
