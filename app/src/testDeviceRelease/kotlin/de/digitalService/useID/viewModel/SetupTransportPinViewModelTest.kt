package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
import de.digitalService.useID.ui.screens.setup.SetupTransportPinNavArgs
import de.digitalService.useID.ui.screens.setup.SetupTransportPinViewModel
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
class SetupTransportPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockChangePinCoordinator: ChangePinCoordinator

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
            mockChangePinCoordinator,
            mockSaveStateHandle
        )

        Assertions.assertEquals(flag, viewModel.retry)
        Assertions.assertEquals(flag, viewModel.identificationPending)
    }

    @Test
    fun testOnDone() {
        val viewModel = SetupTransportPinViewModel(
            mockChangePinCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        verify(exactly = 1) { mockChangePinCoordinator.onOldPinEntered(pin) }
    }

    @Test
    fun testOnNavigationFirstAttempt() {
        every { mockNavArgs.retry } returns false

        val viewModel = SetupTransportPinViewModel(
            mockChangePinCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 1) { mockChangePinCoordinator.onBack() }
        verify(exactly = 0) { mockChangePinCoordinator.cancelPinManagement() }
    }

    @Test
    fun testOnNavigationRetry() {
        every { mockNavArgs.retry } returns true

        val viewModel = SetupTransportPinViewModel(
            mockChangePinCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 0) { mockChangePinCoordinator.onBack() }
        verify(exactly = 1) { mockChangePinCoordinator.cancelPinManagement() }
    }
}
