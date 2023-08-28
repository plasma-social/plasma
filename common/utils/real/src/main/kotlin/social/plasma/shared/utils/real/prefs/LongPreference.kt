package social.plasma.shared.utils.real.prefs

import android.content.SharedPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import social.plasma.shared.utils.api.Preference

class LongPreference @AssistedInject constructor(
    @Assisted override val key: String,
    private val sharedPreferences: SharedPreferences,
) : Preference<Long> {
    override fun get(default: Long?): Long = sharedPreferences.getLong(key, default ?: 0L)

    override fun isSet(): Boolean = sharedPreferences.contains(key)

    override fun remove() = sharedPreferences.edit().remove(key).apply()

    override fun set(value: Long) =
        sharedPreferences.edit().putLong(key, value).apply()

    override fun observe(default: Long?): Flow<Long?> = createObservable(sharedPreferences, default)

    @AssistedFactory
    interface LongPreferenceFactory {
        fun create(key: String): LongPreference
    }
}
