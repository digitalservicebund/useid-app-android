package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInputViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupPersonalPinInputViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @Test
    fun testOnDoneClicked() {
        val viewModel = SetupPersonalPinInputViewModel(pinManagementCoordinator = mockPinManagementCoordinator)

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        verify(exactly = 1) { mockPinManagementCoordinator.setNewPin(pin) }
    }

    @Test
    fun testOnBackClicked() {
        val viewModel = SetupPersonalPinInputViewModel(pinManagementCoordinator = mockPinManagementCoordinator)

        viewModel.onBack()

        verify(exactly = 1) { mockPinManagementCoordinator.onBack() }
    }
}
