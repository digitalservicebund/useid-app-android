package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupCoordinator: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProgressEventCollection() = runTest {
        val scanInProgressFlow = MutableStateFlow(false)
        every { mockPinManagementCoordinator.scanInProgress } returns scanInProgressFlow

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockTrackerManager
        )

        scanInProgressFlow.value = true
        advanceUntilIdle()
        Assertions.assertTrue(viewModel.shouldShowProgress)

        scanInProgressFlow.value = false
        advanceUntilIdle()
        Assertions.assertFalse(viewModel.shouldShowProgress)
    }

    @Test
    fun testOnHelpButtonClicked() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockTrackerManager
        )

        viewModel.onHelpButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackScreen("firstTimeUser/scanHelp") }
    }

    @Test
    fun testOnNfcButtonClicked() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockTrackerManager
        )

        viewModel.onNfcButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo") }
    }

    @Test
    fun testOnNavigationButtonClicked() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockSetupCoordinator,
            mockTrackerManager
        )

        every { mockPinManagementCoordinator.backAllowed } returnsMany listOf(true, false)

        viewModel.onNavigationButtonClicked()

        verify(exactly = 1) { mockPinManagementCoordinator.onBack() }

        viewModel.onNavigationButtonClicked()

        verify(exactly = 1) { mockPinManagementCoordinator.cancelPinManagement() }
    }
}
