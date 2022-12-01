package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @OptIn(ExperimentalCoroutinesApi::class)
    fun collectProgressEvents() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val progressFlow = MutableStateFlow(false)

        every { mockSetupCoordinator.scanInProgress } returns progressFlow

        val viewModel = SetupScanViewModel(
            mockSetupCoordinator,
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
    fun onCancelConfirm() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        every { mockSetupCoordinator.scanInProgress } returns flow { }

        val viewModel = SetupScanViewModel(
            mockSetupCoordinator,
            mockTrackerManager,
            testScope
        )

        viewModel.onCancelConfirm()

        verify(exactly = 1) { mockSetupCoordinator.onBackClicked() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onNfcButtonClicked() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            mockSetupCoordinator,
            mockTrackerManager,
            testScope
        )

        viewModel.onNfcButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onHelpButtonClicked() = runTest {
        val testScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val viewModel = SetupScanViewModel(
            mockSetupCoordinator,
            mockTrackerManager,
            testScope
        )

        viewModel.onHelpButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackScreen("firstTimeUser/scanHelp") }
    }
}
