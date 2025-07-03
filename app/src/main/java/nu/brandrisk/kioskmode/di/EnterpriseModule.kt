package nu.brandrisk.kioskmode.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nu.brandrisk.kioskmode.domain.enterprise.EnterpriseSecurityManager
import nu.brandrisk.kioskmode.domain.enterprise.HardwareControlManager
import nu.brandrisk.kioskmode.domain.enterprise.NetworkManager
import nu.brandrisk.kioskmode.domain.enterprise.XiaomiMIUIManager
import javax.inject.Singleton

/**
 * Enterprise Module
 * Provides dependency injection for enterprise features
 */
@Module
@InstallIn(SingletonComponent::class)
object EnterpriseModule {

    @Provides
    @Singleton
    fun provideEnterpriseSecurityManager(
        securityManager: EnterpriseSecurityManager
    ): EnterpriseSecurityManager = securityManager

    @Provides
    @Singleton
    fun provideNetworkManager(
        networkManager: NetworkManager
    ): NetworkManager = networkManager

    @Provides
    @Singleton
    fun provideHardwareControlManager(
        hardwareManager: HardwareControlManager
    ): HardwareControlManager = hardwareManager

    @Provides
    @Singleton
    fun provideXiaomiMIUIManager(
        xiaomiManager: XiaomiMIUIManager
    ): XiaomiMIUIManager = xiaomiManager
}
