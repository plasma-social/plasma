package social.plasma.opengraph

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OpenGraphModule {
    @Binds
    @Singleton
    internal abstract fun bindsOpenGraphDocumentProvider(impl: RealDocumentProvider): DocumentProvider
}

