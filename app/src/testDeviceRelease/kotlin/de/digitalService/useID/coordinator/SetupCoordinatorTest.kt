package de.digitalService.useID.coordinator

import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.ui.coordinators.*
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockStorageManager: StorageManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(navigationDestinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(navigationPoppingDestinationSlot)) } returns Unit
    }

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.startSetupIdCard()

        verify(exactly = 1) { mockNavigator.navigate(SetupPinLetterDestination) }
    }

    @Test
    fun setupWithPinLetterCanceled() = runTest {

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementFlow = MutableStateFlow(SubCoordinatorState.Active)
        every { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin) } returns pinManagementFlow

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithPinLetter()
        advanceUntilIdle()
        verify(exactly = 1) { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin) }

        pinManagementFlow.value = SubCoordinatorState.Cancelled
        advanceUntilIdle()
        verify(exactly = 1) { setupCoordinator.cancelSetup() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
    }

    @Test
    fun setupWithPinLetterSuccessful() = runTest {

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementFlow = MutableStateFlow(SubCoordinatorState.Active)
        every { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin) } returns pinManagementFlow

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithPinLetter()
        advanceUntilIdle()
        verify(exactly = 1) { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin) }

        pinManagementFlow.value = SubCoordinatorState.Finished
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(SetupFinishDestination) }
    }

    @Test
    fun setupWithoutPinLetter() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithoutPinLetter()

        verify(exactly = 1) { mockNavigator.navigate(SetupResetPersonalPinDestination) }
    }

    @Test
    fun finishSetupNoTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.showSetupIntro(null)
        Assertions.assertEquals(SubCoordinatorState.Active, setupCoordinator.stateFlow.value)
        setupCoordinator.finishSetup()

        verify(exactly = 1) { mockNavigator.popToRoot() }
        Assertions.assertEquals(SubCoordinatorState.Finished, setupCoordinator.stateFlow.value)
    }

    @Test
    fun finishSetupWithTcTokenUrl() = runTest {

        every { mockCoroutineContextProvider.Default } returns dispatcher

        val identificationCoordinatorFlow = MutableStateFlow(SubCoordinatorState.Idle)
        every { mockIdentificationCoordinator.stateFlow } returns identificationCoordinatorFlow

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
        Assertions.assertEquals(SubCoordinatorState.Active, setupCoordinator.stateFlow.value)
        setupCoordinator.finishSetup()

        verify(exactly = 0) { mockNavigator.popToRoot() }
        verify(exactly = 1) { mockIdentificationCoordinator.startIdentificationProcess(testUrl, false) }

        identificationCoordinatorFlow.value = SubCoordinatorState.Finished
        advanceUntilIdle()
        Assertions.assertEquals(false, setupCoordinator.identificationPending)
    }

    @Test
    fun onBack() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.onBackClicked()

        verify(exactly = 1) { mockNavigator.pop() }
    }

    @Test
    fun cancelSetup() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
        Assertions.assertEquals(SubCoordinatorState.Active, setupCoordinator.stateFlow.value)
        setupCoordinator.cancelSetup()

        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Cancelled, setupCoordinator.stateFlow.value)
    }

    @Test
    fun skipSetup() {
        every { mockCoroutineContextProvider.Default } returns dispatcher

        val identificationCoordinatorFlow = MutableStateFlow(SubCoordinatorState.Idle)
        every { mockIdentificationCoordinator.stateFlow } returns identificationCoordinatorFlow

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
        Assertions.assertEquals(SubCoordinatorState.Active, setupCoordinator.stateFlow.value)
        setupCoordinator.skipSetup()

        verify(exactly = 0) { mockNavigator.popToRoot() }
        verify(exactly = 1) { mockIdentificationCoordinator.startIdentificationProcess(testUrl, true) }

        identificationCoordinatorFlow.value = SubCoordinatorState.Finished
    }

    @Test
    fun hasToken() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        Assertions.assertFalse(setupCoordinator.identificationPending)

        setupCoordinator.showSetupIntro(testUrl)

        Assertions.assertTrue(setupCoordinator.identificationPending)
    }
}
