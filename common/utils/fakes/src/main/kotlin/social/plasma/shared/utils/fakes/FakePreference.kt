package social.plasma.shared.utils.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import social.plasma.shared.utils.api.Preference

data class FakePreference<T>(
    override val key: String = "key",
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

    override fun observe(default: T?): Flow<T?> {
        return flowOf(value)
    }
}
