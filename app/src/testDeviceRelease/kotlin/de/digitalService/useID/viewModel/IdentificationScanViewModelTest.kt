package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationScanViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testScanEventCollection() = runTest {
        val scanInProgressFlow = MutableStateFlow(false)
        every { mockIdentificationCoordinator.scanInProgress } returns scanInProgressFlow

        val viewModel = IdentificationScanViewModel(
            mockIdentificationCoordinator,
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
        every { mockIdentificationCoordinator.scanInProgress } returns MutableStateFlow(false)

        val viewModel = IdentificationScanViewModel(
            mockIdentificationCoordinator,
            mockTrackerManager
        )

        viewModel.onHelpButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackScreen("identification/scanHelp") }
    }

    @Test
    fun testOnNfcButtonClicked() = runTest {
        every { mockIdentificationCoordinator.scanInProgress } returns MutableStateFlow(false)

        val viewModel = IdentificationScanViewModel(
            mockIdentificationCoordinator,
            mockTrackerManager
        )

        viewModel.onNfcButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackEvent("identification", "alertShown", "NFCInfo") }
    }

    @Test
    fun testOnCancelIdentification() = runTest {
        every { mockIdentificationCoordinator.scanInProgress } returns MutableStateFlow(false)

        val viewModel = IdentificationScanViewModel(
            mockIdentificationCoordinator,
            mockTrackerManager
        )

        viewModel.onCancelIdentification()

        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
