package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupIntroDestination
import de.digitalService.useID.ui.screens.setup.SetupIntroNavArgs
import de.digitalService.useID.ui.screens.setup.SetupIntroViewModel
import de.digitalService.useID.util.AbTestManager
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class SetupIntroViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockAbTestManager: AbTestManager

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @MockK
    lateinit var mockNavArgs: SetupIntroNavArgs

    @BeforeEach
    fun setup() {
        mockkObject(SetupIntroDestination)
        every { SetupIntroDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.confirmCancellation } returns false
        every { mockAbTestManager.isSetupIntroTestVariation.value } returns false
    }

    @Test
    fun onOnFirstTimeUsage() {
        val viewModel = SetupIntroViewModel(mockSetupCoordinator, mockTrackerManager, mockAbTestManager, mockSaveStateHandle)

        viewModel.onFirstTimeUsage()

        verify(exactly = 1) { mockSetupCoordinator.startSetupIdCard() }
        verify(exactly = 1) { mockTrackerManager.trackButtonPressed("firstTimeUser", "startSetup") }
    }

    @Test
    fun onNonFirstTimeUsage() {
        val viewModel = SetupIntroViewModel(mockSetupCoordinator, mockTrackerManager, mockAbTestManager, mockSaveStateHandle)

        viewModel.onNonFirstTimeUsage()

        verify(exactly = 1) { mockSetupCoordinator.skipSetup() }
        verify(exactly = 1) { mockTrackerManager.trackButtonPressed("firstTimeUser", "alreadySetup") }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testShowVariant(isVariant: Boolean) {
        every { mockAbTestManager.isSetupIntroTestVariation.value } returns isVariant

        val viewModel = SetupIntroViewModel(mockSetupCoordinator, mockTrackerManager, mockAbTestManager, mockSaveStateHandle)

        Assertions.assertEquals(isVariant, viewModel.showVariation)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testConfirmCancellation(confirmCancellation: Boolean) {
        every { mockNavArgs.confirmCancellation } returns confirmCancellation

        val viewModel = SetupIntroViewModel(mockSetupCoordinator, mockTrackerManager, mockAbTestManager, mockSaveStateHandle)

        Assertions.assertEquals(confirmCancellation, viewModel.confirmCancellation)
    }
}
