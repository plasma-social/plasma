package social.plasma.di

import app.cash.molecule.RecompositionClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun providesRecompositionClock(): RecompositionClock = RecompositionClock.ContextClock
}