package social.plasma.shared.utils.real.prefs

import android.content.SharedPreferences
import android.util.Base64
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import social.plasma.shared.utils.api.Preference

class ByteArrayPreference @AssistedInject constructor(
    @Assisted override val key: String,
    private val sharedPreferences: SharedPreferences,
) : Preference<ByteArray> {

    override fun get(default: ByteArray?): ByteArray? {
        val base64String = sharedPreferences.getString(key, null) ?: return default

        return Base64.decode(base64String, Base64.NO_WRAP)
    }

    override fun isSet(): Boolean = sharedPreferences.contains(key)

    override fun remove() = sharedPreferences.edit().remove(key).apply()

    override fun set(value: ByteArray) {
        val base64 = Base64.encodeToString(value, Base64.NO_WRAP)
        sharedPreferences.edit().putString(key, base64).apply()
    }

    override fun observe(default: ByteArray?): Flow<ByteArray?> {
        return createObservable(sharedPreferences, default)
    }

    @AssistedFactory
    interface ByteArrayPreferenceFactory {
        fun create(key: String): ByteArrayPreference
    }
}
