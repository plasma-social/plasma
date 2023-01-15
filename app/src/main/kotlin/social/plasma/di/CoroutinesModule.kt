package social.plasma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Named("default")
    fun providesDefaultDispatcher(): CoroutineContext = Dispatchers.Default
}
