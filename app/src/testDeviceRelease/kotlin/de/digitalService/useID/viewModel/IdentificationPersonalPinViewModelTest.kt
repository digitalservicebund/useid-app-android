package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinNavArgs
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationPersonalPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @Test
    fun testNavArgsAssignedOnInit() {
        val mockNavArgs: IdentificationPersonalPinNavArgs = mockk()
        mockkObject(IdentificationPersonalPinDestination)
        every { IdentificationPersonalPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        Assertions.assertTrue(viewModel.retry)
    }

    @Test
    fun testOnDone() {
        val mockNavArgs: IdentificationPersonalPinNavArgs = mockk()
        mockkObject(IdentificationPersonalPinDestination)
        every { IdentificationPersonalPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDone(pin)

        verify(exactly = 1) { mockIdentificationCoordinator.setPin(pin) }
    }

    @Test
    fun testOnNavigationFirstAttempt() {
        val mockNavArgs: IdentificationPersonalPinNavArgs = mockk()
        mockkObject(IdentificationPersonalPinDestination)
        every { IdentificationPersonalPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns false

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDone(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 1) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 0) { mockIdentificationCoordinator.cancelIdentification() }
    }

    @Test
    fun testOnNavigationRetry() {
        val mockNavArgs: IdentificationPersonalPinNavArgs = mockk()
        mockkObject(IdentificationPersonalPinDestination)
        every { IdentificationPersonalPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.retry } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        val pin = "111111"
        viewModel.onDone(pin)

        viewModel.onNavigationButtonClicked()
        verify(exactly = 0) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
