package de.digitalService.useID

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.composables.screens.SetupScanViewModel
import de.digitalService.useID.ui.coordinators.SetupScanCoordinator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK
    lateinit var coordinator: SetupScanCoordinator

    @MockK
    lateinit var idCardManager: IDCardManager

    @MockK
    lateinit var savedStateHandle: SavedStateHandle

    @MockK
    lateinit var context: Context

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun scanSuccess() = runTest {
        val viewModelScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { savedStateHandle.get<String>("TransportPIN") } returns transportPIN
        every { savedStateHandle.get<String>("PersonalPIN") } returns personalPIN

        val viewModel = SetupScanViewModel(coordinator, idCardManager, viewModelScope, savedStateHandle)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPIN, personalPIN) } just Runs

        every { idCardManager.changePin(context) } returns flow {
            emit(EIDInteractionEvent.PINManagementStarted)
            emit(EIDInteractionEvent.RequestChangedPIN(attempts = null, pinCallback))
            emit(EIDInteractionEvent.ProcessCompletedSuccessfully)
        }

        viewModel.startSettingPIN(context)

        advanceUntilIdle()

        verify { pinCallback(transportPIN, personalPIN) }
        verify { coordinator.settingPINSucceeded() }
    }
}
