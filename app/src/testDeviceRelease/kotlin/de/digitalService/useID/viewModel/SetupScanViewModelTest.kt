package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
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
    lateinit var mockChangePinCoordinator: ChangePinCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @MockK
    lateinit var mockNavArgs: SetupScanNavArgs

    private val dispatcher = StandardTestDispatcher()

    private val scanInProgressFlow = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        mockkObject(SetupScanDestination)
        every { SetupScanDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationPending } returns true
        every { mockNavArgs.backAllowed } returns true
        every { mockChangePinCoordinator.scanInProgress } returns scanInProgressFlow

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
            mockChangePinCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        Assertions.assertEquals(flag, viewModel.identificationPending)
        Assertions.assertEquals(flag, viewModel.backAllowed)
    }

    @Test
    fun testProgressEventCollection() = runTest {
        val viewModel = SetupScanViewModel(
            mockChangePinCoordinator,
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
        val viewModel = SetupScanViewModel(
            mockChangePinCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onHelpButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackScreen("firstTimeUser/scanHelp") }
    }

    @Test
    fun testOnNfcButtonClicked() = runTest {
        val viewModel = SetupScanViewModel(
            mockChangePinCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNfcButtonClicked()

        verify(exactly = 1) { mockTrackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo") }
    }

    @Test
    fun testOnNavigationButtonClickedBackAllowed() = runTest {
        every { mockNavArgs.backAllowed } returns true

        val viewModel = SetupScanViewModel(
            mockChangePinCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify { mockChangePinCoordinator.onBack() }
    }

    @Test
    fun testOnNavigationButtonClickedBackNotAllowed() = runTest {
        every { mockNavArgs.backAllowed } returns false

        val viewModel = SetupScanViewModel(
            mockChangePinCoordinator,
            mockTrackerManager,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify { mockChangePinCoordinator.cancelPinManagement() }
    }
}
