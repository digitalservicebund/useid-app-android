package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import de.digitalService.useID.ui.screens.identification.ScanEvent
import de.digitalService.useID.util.CoroutineContextProviderType
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationScanViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProviderType

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        every { mockCoroutineContextProvider.IO } returns dispatcher
        every { mockCoroutineContextProvider.Main } returns dispatcher
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun collectScanEvents_ScanEventCardRequested() = runTest {
        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.CardRequested)
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        runCurrent()

        Assertions.assertFalse(viewModel.shouldShowProgress)
    }

    @Test
    fun collectScanEvents_ScanEventCardAttached() = runTest {
        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.CardAttached)
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        runCurrent()

        Assertions.assertTrue(viewModel.shouldShowProgress)
    }

    @Test
    fun collectScanEvents_ScanErrorFinished() = runTest {
        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.Finished)
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        runCurrent()

        Assertions.assertFalse(viewModel.shouldShowProgress)
    }

    @Test
    fun collectScanEvents_ScanErrorOther() = runTest {
        val testError = ScanError.Other(null)

        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.Error(testError))
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        runCurrent()

        Assertions.assertFalse(viewModel.shouldShowProgress)
        Assertions.assertEquals(testError, viewModel.errorState)
    }

    @Test
    fun onCancelIdentification() = runTest {
        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.CardAttached)
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        viewModel.onCancelIdentification()
        runCurrent()

        verify(exactly = 1) { mockCoordinator.cancelIdentification() }
    }

    @Test
    fun onNewPersonalPINEntered() = runTest {
        val testError = ScanError.Other(null)
        val testPin = "123456"

        every { mockCoordinator.scanEventFlow } returns flow {
            emit(ScanEvent.Error(testError))
        }

        val viewModel = IdentificationScanViewModel(
            mockCoordinator,
            mockCoroutineContextProvider,
            mockTrackerManager
        )

        runCurrent()

        Assertions.assertEquals(testError, viewModel.errorState)

        viewModel.onNewPersonalPINEntered(testPin)

        Assertions.assertEquals(null, viewModel.errorState)

        verify(exactly = 1) { mockCoordinator.onPINEntered(testPin) }
    }
}
