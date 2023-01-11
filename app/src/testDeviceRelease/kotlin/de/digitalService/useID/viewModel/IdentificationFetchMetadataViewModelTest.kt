package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataNavArgs
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationFetchMetadataViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @Test
    fun testNavArgsAssignedOnInit() {
        val mockNavArgs: IdentificationFetchMetadataNavArgs = mockk()
        mockkObject(IdentificationFetchMetadataDestination)
        every { IdentificationFetchMetadataDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.backAllowed } returns true

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        assertTrue(viewModel.backAllowed)
    }

    @Test
    fun testOnNavigationWithoutSetup() {
        val mockNavArgs: IdentificationFetchMetadataNavArgs = mockk()
        mockkObject(IdentificationFetchMetadataDestination)
        every { IdentificationFetchMetadataDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        val identificationCoordinatorSkippedSetup = true
        every { mockNavArgs.backAllowed } returns identificationCoordinatorSkippedSetup

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()
        verify(exactly = 1) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 0) { mockIdentificationCoordinator.cancelIdentification() }
    }

    @Test
    fun testOnNavigationWithSetup() {
        val mockNavArgs: IdentificationFetchMetadataNavArgs = mockk()
        mockkObject(IdentificationFetchMetadataDestination)
        every { IdentificationFetchMetadataDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        val identificationCoordinatorSkippedSetup = false
        every { mockNavArgs.backAllowed } returns identificationCoordinatorSkippedSetup

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()
        verify(exactly = 0) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
