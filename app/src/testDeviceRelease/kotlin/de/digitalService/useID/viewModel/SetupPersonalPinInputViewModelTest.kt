package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
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
    lateinit var mockChangePinCoordinator: ChangePinCoordinator

    @Test
    fun testOnDoneClicked() {
        val viewModel = SetupPersonalPinInputViewModel(changePinCoordinator = mockChangePinCoordinator)

        val pin = "111111"
        viewModel.onDoneClicked(pin)

        verify(exactly = 1) { mockChangePinCoordinator.onNewPinEntered(pin) }
    }

    @Test
    fun testOnBackClicked() {
        val viewModel = SetupPersonalPinInputViewModel(changePinCoordinator = mockChangePinCoordinator)

        viewModel.onBack()

        verify(exactly = 1) { mockChangePinCoordinator.onBack() }
    }
}
