package social.plasma.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class ApplicationResumedLifecycle(
    application: Application,
    private val lifecycleRegistry: LifecycleRegistry,
) : Lifecycle by lifecycleRegistry {

    init {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private inner class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        init {
            lifecycleStarted()
        }

        override fun onActivityPaused(activity: Activity) = lifecycleRegistry.onNext(
            Lifecycle.State.Stopped.WithReason(ShutdownReason(1000, "App is paused"))
        )

        override fun onActivityResumed(activity: Activity) =
            lifecycleStarted()

        private fun lifecycleStarted() {
            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityDestroyed(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    }
}
