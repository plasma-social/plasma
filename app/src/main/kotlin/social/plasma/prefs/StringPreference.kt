package social.plasma.prefs

import android.content.SharedPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class StringPreference @AssistedInject constructor(
    @Assisted private val key: String,
    private val sharedPreferences: SharedPreferences,
) : Preference<String> {
    override fun get(default: String?): String? = sharedPreferences.getString(key, default)

    override fun isSet(): Boolean = sharedPreferences.contains(key)

    override fun remove() = sharedPreferences.edit().remove(key).apply()

    override fun set(value: String) = sharedPreferences.edit().putString(key, value).apply()

    @AssistedFactory
    interface StringPreferenceFactory {
        fun create(key: String): StringPreference
    }
}
