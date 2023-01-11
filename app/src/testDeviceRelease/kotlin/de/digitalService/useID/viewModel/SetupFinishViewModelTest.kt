package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupFinishViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupFinishViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @Test
    fun testIdentificationPending() {
        val viewModel = SetupFinishViewModel(mockSetupCoordinator)

        every { mockSetupCoordinator.identificationPending } returns false
        Assertions.assertFalse(viewModel.identificationPending)

        every { mockSetupCoordinator.identificationPending } returns true
        Assertions.assertTrue(viewModel.identificationPending)
    }

    @Test
    fun onOnButtonClicked() {
        val viewModel = SetupFinishViewModel(mockSetupCoordinator)

        viewModel.onButtonClicked()
        
        verify(exactly = 1) { mockSetupCoordinator.finishSetup() }
    }
}
