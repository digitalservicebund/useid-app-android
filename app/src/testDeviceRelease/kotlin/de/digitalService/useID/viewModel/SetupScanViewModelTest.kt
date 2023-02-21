package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupScanDestination
import de.digitalService.useID.ui.screens.setup.SetupScanNavArgs
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {
    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @MockK
    lateinit var mockNavArgs: SetupScanNavArgs

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        mockkObject(SetupScanDestination)
        every { SetupScanDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationPending } returns true
        every { mockNavArgs.backAllowed } returns true
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testNavArgsAssignedOnInit(flag: Boolean) = runTest {
        every { mockNavArgs.identificationPending } returns flag
        every { mockNavArgs.backAllowed } returns flag

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        Assertions.assertEquals(flag, viewModel.identificationPending)
        Assertions.assertEquals(flag, viewModel.backAllowed)
    }

    @Test
    fun testProgressEventCollection() = runTest {
        val scanInProgressFlow = MutableStateFlow(false)
        every { mockPinManagementCoordinator.scanInProgress } returns scanInProgressFlow

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
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
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onHelpButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackScreen("firstTimeUser/scanHelp") }
    }

    @Test
    fun testOnNfcButtonClicked() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNfcButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo") }
    }

    @Test
    fun testOnNavigationButtonClickedBackAllowed() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()
        every { mockNavArgs.backAllowed } returns true

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify { mockPinManagementCoordinator.onBack() }
    }

    @Test
    fun testOnNavigationButtonClickedBackNotAllowed() = runTest {

        every { mockPinManagementCoordinator.scanInProgress } returns mockk()
        every { mockNavArgs.backAllowed } returns false

        val viewModel = SetupScanViewModel(
            mockPinManagementCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify { mockPinManagementCoordinator.cancelPinManagement() }
    }
}
