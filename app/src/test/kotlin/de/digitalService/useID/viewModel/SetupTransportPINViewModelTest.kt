package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupTransportPINViewModel
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupTransportPINViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var coordinatorMock: SetupCoordinator

    private val defaultPin = ""

    @Test
    fun onInputChanged() {
        val testValue = "12345"

        val viewModel = SetupTransportPINViewModel(
            coordinatorMock
        )

        viewModel.onInputChanged(testValue)

        Assertions.assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { coordinatorMock.onTransportPINEntered(any()) }
    }

    @Test
    fun onDoneTapped_Success() {
        val testValue = "12345"

        val viewModel = SetupTransportPINViewModel(
            coordinatorMock
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(defaultPin, viewModel.transportPIN)

        verify(exactly = 1) { coordinatorMock.onTransportPINEntered(testValue) }
    }

    @Test
    fun onDoneTapped_NoPreviousInput() {
        val viewModel = SetupTransportPINViewModel(
            coordinatorMock
        )

        viewModel.onDoneTapped()

        Assertions.assertEquals(defaultPin, viewModel.transportPIN)

        verify(exactly = 0) { coordinatorMock.onTransportPINEntered(any()) }
    }

    @Test
    fun onDoneTapped_TooShort() {
        val testValue = "1234"

        val viewModel = SetupTransportPINViewModel(
            coordinatorMock
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { coordinatorMock.onTransportPINEntered(any()) }
    }

    @Test
    fun onDoneTapped_TooLong() {
        val testValue = "123456"

        val viewModel = SetupTransportPINViewModel(
            coordinatorMock
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { coordinatorMock.onTransportPINEntered(any()) }
    }
}
