package social.plasma.shared.utils.api

import android.content.SharedPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

interface Preference<T> {
    val key: String

    fun get(default: T?): T?

    fun set(value: T)

    fun isSet(): Boolean

    fun remove()

    fun observe(default: T? = null): Flow<T?>

    fun createObservable(
        sharedPref: SharedPreferences,
        default: T? = null,
    ): Flow<T?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (key == prefKey) {
                trySend(get(default))
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)

        if (isSet()) {
            send(get(default))
        } else {
            send(default)
        }
        awaitClose { sharedPref.unregisterOnSharedPreferenceChangeListener(listener) }
    }.buffer(Channel.UNLIMITED)
}
