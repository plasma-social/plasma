package social.plasma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Named("default")
    fun providesDefaultDispatcher(): CoroutineContext = Dispatchers.Default

    @Provides
    @Named("io")
    fun providesIODispatcher(): CoroutineContext = Dispatchers.IO


}

@Module
@InstallIn(ActivityRetainedComponent::class)
object CoroutineScopeModule {

    @Provides
    @ActivityRetainedScoped
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}


