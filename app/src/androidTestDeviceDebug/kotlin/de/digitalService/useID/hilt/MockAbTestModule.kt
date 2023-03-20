package de.digitalService.useID.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import de.digitalService.useID.util.AbTestManager
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AbTestModule::class]
)
class MockAbTestModule {

    @Provides
    @Singleton
    fun providesAbTestManager(): AbTestManager = mockk(relaxed = true) {
        every { isSetupIntroTestVariant.value } returns false
    }
}
