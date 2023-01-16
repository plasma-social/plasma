package social.plasma.prefs

import android.content.SharedPreferences
import android.util.Base64
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ByteArrayPreference @AssistedInject constructor(
    @Assisted private val key: String,
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

    @AssistedFactory
    interface ByteArrayPreferenceFactory {
        fun create(key: String): ByteArrayPreference
    }
}
