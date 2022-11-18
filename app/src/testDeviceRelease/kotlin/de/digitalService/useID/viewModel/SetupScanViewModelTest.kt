package de.digitalService.useID.viewModel

import android.content.Context
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK(relaxUnitFun = true)
    lateinit var coordinatorMock: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @OptIn(ExperimentalCoroutinesApi::class)
    fun collectProgressEvents() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val progressFlow = MutableStateFlow(false)

        every { coordinatorMock.scanInProgress } returns progressFlow

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            mockTrackerManager,
            testScope
        )

        Assertions.assertFalse(viewModel.shouldShowProgress)

        progressFlow.value = true
        advanceUntilIdle()

        Assertions.assertTrue(viewModel.shouldShowProgress)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onBack() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            mockTrackerManager,
            testScope
        )

        viewModel.onCancelConfirm()

        verify(exactly = 1) { coordinatorMock.cancelSetup() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onCancel() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            mockTrackerManager,
            testScope
        )

        viewModel.onCancelConfirm()

        verify(exactly = 1) { coordinatorMock.cancelSetup() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onNfcButtonTapped() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            mockTrackerManager,
            testScope
        )

        viewModel.onNfcButtonTapped()

        verify(exactly = 1) { mockTrackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onHelpButtonTapped() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            coordinatorMock,
            mockTrackerManager,
            testScope
        )

        viewModel.onHelpButtonTapped()

        verify(exactly = 1) { mockTrackerManager.trackScreen("firstTimeUser/scanHelp") }
    }
}
