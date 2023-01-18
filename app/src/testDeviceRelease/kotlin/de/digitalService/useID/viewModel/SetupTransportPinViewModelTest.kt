package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinNavArgs
import de.digitalService.useID.ui.screens.setup.SetupTransportPinNavArgs
import de.digitalService.useID.ui.screens.setup.SetupTransportPinViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupTransportPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @Test
    fun testNavArgsAssignedOnInit() {
        val mockNavArgs: SetupTransportPinNavArgs = mockk()
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockSaveStateHandle
        )

        Assertions.assertTrue(viewModel.retry)
    }

    @Test
    fun testOnDone() {
        val mockNavArgs: SetupTransportPinNavArgs = mockk()
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        verify(exactly = 1) { mockPinManagementCoordinator.setOldPin(pin) }
    }

    @Test
    fun testOnNavigationFirstAttempt() {
        val mockNavArgs: SetupTransportPinNavArgs = mockk()
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns false

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 1) { mockPinManagementCoordinator.onBack() }
        verify(exactly = 0) { mockPinManagementCoordinator.cancelPinManagement() }
    }

    @Test
    fun testOnNavigationRetry() {
        val mockNavArgs: SetupTransportPinNavArgs = mockk()
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 0) { mockPinManagementCoordinator.onBack() }
        verify(exactly = 1) { mockPinManagementCoordinator.cancelPinManagement() }
    }
}
