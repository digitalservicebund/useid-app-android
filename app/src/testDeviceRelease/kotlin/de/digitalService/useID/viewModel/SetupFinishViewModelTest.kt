package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataNavArgs
import de.digitalService.useID.ui.screens.setup.SetupFinishNavArgs
import de.digitalService.useID.ui.screens.setup.SetupFinishViewModel
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
class SetupFinishViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @MockK
    lateinit var mockNavArgs: SetupFinishNavArgs

    @BeforeEach
    fun setup() {
        mockkObject(SetupFinishDestination)
        every { SetupFinishDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationPending } returns true
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testIdentificationPending(identificationPending: Boolean) {
        every { mockNavArgs.identificationPending } returns identificationPending

        val viewModel = SetupFinishViewModel(mockSetupCoordinator, mockSaveStateHandle)

        Assertions.assertEquals(identificationPending, viewModel.identificationPending)
    }

    @Test
    fun onOnButtonClicked() {
        val viewModel = SetupFinishViewModel(mockSetupCoordinator, mockSaveStateHandle)

        viewModel.onButtonClicked()

        verify(exactly = 1) { mockSetupCoordinator.onSetupFinishConfirmed() }
    }
}
