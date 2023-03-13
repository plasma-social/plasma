package social.plasma.utils

import android.util.Log
import io.sentry.Sentry
import io.sentry.SentryEvent
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        if (t != null) {
            if (priority == Log.ERROR) {
                Sentry.captureException(t)
            } else if (priority == Log.WARN) {
                Sentry.captureEvent(SentryEvent(t))
            }
        }
    }
}
