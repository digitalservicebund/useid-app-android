package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinConfirmViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupPersonalPinConfirmViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @Test
    fun testShouldShowErrorFalse() {

        val viewModel = SetupPersonalPinConfirmViewModel(mockPinManagementCoordinator)

        val pin = "111111"
        every { mockPinManagementCoordinator.confirmNewPin(pin) } returns true

        viewModel.onDoneClicked(pin)
        verify(exactly = 1) { mockPinManagementCoordinator.confirmNewPin(pin) }
        Assertions.assertFalse(viewModel.shouldShowError)
    }

    @Test
    fun testShouldShowErrorTrue() {

        val viewModel = SetupPersonalPinConfirmViewModel(mockPinManagementCoordinator)

        val pin = "111111"
        every { mockPinManagementCoordinator.confirmNewPin(pin) } returns false

        viewModel.onDoneClicked(pin)
        verify(exactly = 1) { mockPinManagementCoordinator.confirmNewPin(pin) }
        Assertions.assertTrue(viewModel.shouldShowError)
    }

    @Test
    fun testOnErrorDialogButtonClicked() {

        val viewModel = SetupPersonalPinConfirmViewModel(mockPinManagementCoordinator)

        viewModel.onErrorDialogButtonClicked()
        verify(exactly = 1) { mockPinManagementCoordinator.onConfirmPinMismatchError() }
    }

    @Test
    fun testOnBackClicked() {

        val viewModel = SetupPersonalPinConfirmViewModel(mockPinManagementCoordinator)

        viewModel.onBack()
        verify(exactly = 1) { mockPinManagementCoordinator.onBack() }
    }
}
