package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
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
    lateinit var coordinatorMock: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    private val mockNavArgs: SetupTransportPinNavArgs = mockk()
    private val defaultPin = ""

    @BeforeEach
    fun beforeEach() {
        mockkObject(SetupTransportPinDestination)
        every { SetupTransportPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs

        every { mockNavArgs.attempts } returns null
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 5, 8, 13, -123])
    fun readNavArgsCorrect_Ints(testValue: Int) {
        every { mockNavArgs.attempts } returns testValue

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        Assertions.assertEquals(testValue, viewModel.attempts)
    }

    @Test
    fun readNavArgsCorrect_Null() {
        every { mockNavArgs.attempts } returns null

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        Assertions.assertNull(viewModel.attempts)
    }

    @Test
    fun onInputChanged() {
        val testValue = "12345"

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        viewModel.onInputChanged(testValue)

        Assertions.assertEquals(testValue, viewModel.transportPin)

        verify(exactly = 0) { coordinatorMock.onTransportPinEntered(any()) }
    }

    @Test
    fun onDoneTapped_Success() {
        val testValue = "12345"

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(defaultPin, viewModel.transportPin)

        verify(exactly = 1) { coordinatorMock.onTransportPinEntered(testValue) }
    }

    @Test
    fun onDoneTapped_NoPreviousInput() {
        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        viewModel.onDoneTapped()

        Assertions.assertEquals(defaultPin, viewModel.transportPin)

        verify(exactly = 0) { coordinatorMock.onTransportPinEntered(any()) }
    }

    @Test
    fun onDoneTapped_TooShort() {
        val testValue = "1234"

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(testValue, viewModel.transportPin)

        verify(exactly = 0) { coordinatorMock.onTransportPinEntered(any()) }
    }

    @Test
    fun onDoneTapped_TooLong() {
        val testValue = "123456"

        val viewModel = SetupTransportPinViewModel(
            coordinatorMock,
            mockSaveStateHandle
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        Assertions.assertEquals(testValue, viewModel.transportPin)

        verify(exactly = 0) { coordinatorMock.onTransportPinEntered(any()) }
    }
}
