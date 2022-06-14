package de.digitalService.useID

import android.content.Context
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.composables.screens.SetupScanViewModel
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK(relaxed = true)
    lateinit var coordinatorMock: SetupCoordinator

    @MockK
    lateinit var secureStorageManagerMock: SecureStorageManager

    @MockK
    lateinit var idCardManagerMock: IDCardManager

    @MockK
    lateinit var contextMock: Context

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_Success() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPIN, personalPIN) } just Runs

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            emit(EIDInteractionEvent.PINManagementStarted)
            emit(EIDInteractionEvent.RequestChangedPIN(attempts = null, pinCallback))
            emit(EIDInteractionEvent.ProcessCompletedSuccessfully)
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 1) { pinCallback(transportPIN, personalPIN) }
        verify(exactly = 1) { coordinatorMock.onSettingPINSucceeded() }
        verify(exactly = 1) { secureStorageManagerMock.loadPersonalPIN() }
        verify(exactly = 1) { secureStorageManagerMock.loadTransportPIN() }

        assertNull(viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_NoTransportPin() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = null
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 0) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }
        assertEquals(SetupScanViewModelInterface.Error.Other(null), viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_NoPersonalPin() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = null
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 0) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }
        assertEquals(SetupScanViewModelInterface.Error.Other(null), viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_CardDeactivated() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            throw IDCardInteractionException.CardDeactivated
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertEquals(SetupScanViewModelInterface.Error.IDDeactivated, viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_CardBlocked() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            throw IDCardInteractionException.CardBlocked
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertEquals(SetupScanViewModelInterface.Error.PINBlocked, viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_OtherException() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val exception_message = "exception_message"
        every { idCardManagerMock.changePin(contextMock) } returns flow {
            throw Exception(exception_message)
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertEquals(SetupScanViewModelInterface.Error.Other(exception_message), viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_RequestChangePinTwice() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPIN, personalPIN) } just Runs

        val attempts = 2
        every { idCardManagerMock.changePin(contextMock) } returns flow {
            emit(EIDInteractionEvent.PINManagementStarted)
            emit(EIDInteractionEvent.RequestChangedPIN(attempts = null, pinCallback))
            emit(EIDInteractionEvent.RequestChangedPIN(attempts = attempts, pinCallback))
            emit(EIDInteractionEvent.ProcessCompletedSuccessfully)
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 1) { pinCallback(transportPIN, personalPIN) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertNull(viewModel.errorState)
        assertEquals(attempts, viewModel.attempts)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_RequestCANAndChangedPIN() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val pinCallback = mockk<(String, String, String) -> Unit>()

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            emit(EIDInteractionEvent.RequestCANAndChangedPIN(pinCallback))
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { pinCallback(any(), any(), any()) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertEquals(SetupScanViewModelInterface.Error.PINSuspended, viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startSettingPIN_RequestPUK() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadTransportPIN() } returns transportPIN
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val pinCallback = mockk<(String) -> Unit>()

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            emit(EIDInteractionEvent.RequestPUK(pinCallback))
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.startSettingPIN(contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { pinCallback(any()) }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }

        assertEquals(SetupScanViewModelInterface.Error.PINBlocked, viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onReEnteredTransportPIN() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val transportPIN = "12345"
        val personalPIN = "123456"
        every { secureStorageManagerMock.loadPersonalPIN() } returns personalPIN

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPIN, personalPIN) } just Runs

        every { idCardManagerMock.changePin(contextMock) } returns flow {
            emit(EIDInteractionEvent.PINManagementStarted)
            emit(EIDInteractionEvent.RequestChangedPIN(attempts = null, pinCallback))
            emit(EIDInteractionEvent.ProcessCompletedSuccessfully)
        }

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.onReEnteredTransportPIN(transportPIN, contextMock)

        advanceUntilIdle()

        verify(exactly = 1) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 1) { pinCallback(transportPIN, personalPIN) }
        verify(exactly = 1) { coordinatorMock.onSettingPINSucceeded() }
        verify(exactly = 1) { secureStorageManagerMock.loadPersonalPIN() }
        verify(exactly = 0) { secureStorageManagerMock.loadTransportPIN() }

        assertNull(viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onErrorDialogButtonTap() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.onErrorDialogButtonTap()

        verify(exactly = 1) { coordinatorMock.cancelSetup() }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }
        verify(exactly = 0) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { secureStorageManagerMock.loadPersonalPIN() }
        verify(exactly = 0) { secureStorageManagerMock.loadTransportPIN() }

        assertNull(viewModel.errorState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onCancel() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.onCancel()

        verify(exactly = 1) { coordinatorMock.cancelSetup() }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }
        verify(exactly = 0) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { secureStorageManagerMock.loadPersonalPIN() }
        verify(exactly = 0) { secureStorageManagerMock.loadTransportPIN() }

        assertNull(viewModel.errorState)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onHelpButtonTapped() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            secureStorageManagerMock,
            idCardManagerMock,
            testScope
        )

        viewModel.onHelpButtonTapped()

        verify(exactly = 0) { coordinatorMock.cancelSetup() }
        verify(exactly = 0) { coordinatorMock.onSettingPINSucceeded() }
        verify(exactly = 0) { idCardManagerMock.changePin(contextMock) }
        verify(exactly = 0) { secureStorageManagerMock.loadPersonalPIN() }
        verify(exactly = 0) { secureStorageManagerMock.loadTransportPIN() }

        assertNull(viewModel.errorState)
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun fail() = runTest {
//        Assertions.fail()
//    }
}
