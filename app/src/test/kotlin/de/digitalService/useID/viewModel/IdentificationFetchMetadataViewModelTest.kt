package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.composables.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadataViewModel
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_collectMetaDataEvents_started() {
        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns
            MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
        )

        dispatcher.scheduler.advanceUntilIdle();

        assertFalse(viewModel.shouldShowError)
        assertTrue(viewModel.shouldShowProgressIndicator)
    }

    @Test
    fun init_collectMetaDataEvents_error() {
        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns
            MutableStateFlow(FetchMetadataEvent.Error)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
        )

        dispatcher.scheduler.advanceUntilIdle();

        assertTrue(viewModel.shouldShowError)
        assertFalse(viewModel.shouldShowProgressIndicator)
    }

    @Test
    fun init_collectMetaDataEvents_finished() {
        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns
            MutableStateFlow(FetchMetadataEvent.Finished)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
        )

        dispatcher.scheduler.advanceUntilIdle();

        assertFalse(viewModel.shouldShowError)
        assertFalse(viewModel.shouldShowProgressIndicator)
    }

    @Test
    fun fetchMetaDataEvents() {
        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns
            MutableStateFlow(FetchMetadataEvent.Started)

        val viewModel = IdentificationFetchMetadataViewModel(
            mockIdentificationCoordinator,
        )

        viewModel.fetchMetadata()

        verify(exactly = 1) { mockIdentificationCoordinator.startIdentificationProcess() }
    }
}
