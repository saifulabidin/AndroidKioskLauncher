package nu.brandrisk.kioskmode.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Enterprise Module
 * Enterprise classes are auto-provided by @Inject constructor + @Singleton
 * No manual providers needed - this prevents circular dependency
 */
@Module
@InstallIn(SingletonComponent::class)
object EnterpriseModule {
    // All enterprise classes use @Inject constructor + @Singleton
    // so they are automatically available to Hilt without manual providers
}
