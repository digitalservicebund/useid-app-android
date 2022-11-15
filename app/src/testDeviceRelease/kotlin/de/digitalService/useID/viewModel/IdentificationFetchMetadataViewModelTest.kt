package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataNavArgs
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationFetchMetadataViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private var mockNavArgs: IdentificationFetchMetadataNavArgs = mockk()
    private val testURL = "bundesident://127.0.0.1/eID-Client?tokenURL="

    lateinit var savedStateHandle: SavedStateHandle

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        savedStateHandle = SavedStateHandle()
        savedStateHandle["tcTokenURL"] = testURL
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_collectMetaDataEvents_init_didSetupTrue() {
        val testDidSetup = true
        savedStateHandle["didSetup"] = testDidSetup

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            savedStateHandle
        )

        assertTrue(viewModel.didSetup)
    }

    @Test
    fun init_collectMetaDataEvents_init_didSetupFalse() {
        val testDidSetup = false
        savedStateHandle["didSetup"] = testDidSetup

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            savedStateHandle
        )

        assertFalse(viewModel.didSetup)
    }

    @Test
    fun init_collectMetaDataEvents_startIdentificationProcess_didSetupTrue() {
        val testDidSetup = true
        savedStateHandle["didSetup"] = testDidSetup

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            savedStateHandle
        )
        viewModel.startIdentificationProcess()

        verify { mockIdentificationCoordinator.startIdentificationProcess(testURL, testDidSetup) }
    }

    @Test
    fun init_collectMetaDataEvents_startIdentificationProcess_didSetupFalse() {
        val testDidSetup = false
        savedStateHandle["didSetup"] = testDidSetup

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            savedStateHandle
        )
        viewModel.startIdentificationProcess()

        verify { mockIdentificationCoordinator.startIdentificationProcess(testURL, testDidSetup) }
    }

    @Test
    fun onCancelButtonTapped() {
        val testDidSetup = false
        savedStateHandle["didSetup"] = testDidSetup

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns
            MutableStateFlow(FetchMetadataEvent.Finished)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
            savedStateHandle
        )

        viewModel.onCancelButtonTapped()

        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
