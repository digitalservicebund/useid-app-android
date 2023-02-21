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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class SetupTransportPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @MockK
    lateinit var mockNavArgs: SetupTransportPinNavArgs

    @BeforeEach
    fun setup() {
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true
        every { mockNavArgs.identificationPending } returns true
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testNavArgsAssignedOnInit(flag: Boolean) {
        every { mockNavArgs.retry } returns flag
        every { mockNavArgs.identificationPending } returns flag

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSaveStateHandle
        )

        Assertions.assertEquals(flag, viewModel.retry)
        Assertions.assertEquals(flag, viewModel.identificationPending)
    }

    @Test
    fun testOnDone() {
        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        verify(exactly = 1) { mockPinManagementCoordinator.onOldPinEntered(pin) }
    }

    @Test
    fun testOnNavigationFirstAttempt() {
        every { mockNavArgs.retry } returns false

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
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
        every { mockNavArgs.retry } returns true

        val viewModel = SetupTransportPinViewModel(
            mockPinManagementCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 0) { mockPinManagementCoordinator.onBack() }
        verify(exactly = 1) { mockPinManagementCoordinator.cancelPinManagement() }
    }
}
