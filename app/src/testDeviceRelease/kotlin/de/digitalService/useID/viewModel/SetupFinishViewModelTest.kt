package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupFinishViewModel
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupFinishViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockCoordinator: SetupCoordinator

    @Test
    fun onCloseButtonClicked() {
        val viewModel = SetupFinishViewModel(mockCoordinator)

//        viewModel.onCloseButtonClicked()

//        verify(exactly = 1) { mockCoordinator.onBackToHome() }
    }

    @Test
    fun onIdentifyButtonClicked() {
        val viewModel = SetupFinishViewModel(mockCoordinator)

        viewModel.onButtonTapped()

        verify(exactly = 1) { mockCoordinator.finishSetup() }
    }
}
