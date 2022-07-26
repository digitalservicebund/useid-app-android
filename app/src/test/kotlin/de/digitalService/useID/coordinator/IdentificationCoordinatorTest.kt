package de.digitalService.useID.coordinator

import android.content.Context
import android.net.Uri
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationSuccessDestination
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIDCardManager: IDCardManager

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
    fun startIdentificationProcess() {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow(EIDInteractionEvent.ProcessCompletedSuccessfully(testRedirectUrl))

        every { mockIDCardManager.identify(mockContext, any()) } returns testFlow

        mockkStatic("android.net.Uri")
        every { Uri.decode(any()) } returns testRedirectUrl

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager
        )

        identificationCoordinator.startIdentificationProcess()

        dispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationSuccessDestination(testRedirectUrl)) }
    }
}
