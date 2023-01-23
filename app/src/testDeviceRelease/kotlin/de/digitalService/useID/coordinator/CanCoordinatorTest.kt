package de.digitalService.useID.coordinator

import android.net.Uri
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.can.IdentificationCanPinForgotten
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import de.jodamob.junit5.DefaultTypeFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.reflect.KClass

@ExtendWith(MockKExtension::class)
class CanCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdCardManager: IdCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val destinationSlot = slot<Direction>()

    private val can = "123456"
    private val pin = "000000"
    private val newPin = "000000"

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        every { mockAppNavigator.navigate(capture(destinationSlot)) } returns Unit
        every { mockCoroutineContextProvider.IO } returns dispatcher

        // For supporting destinations with String nav arguments
        mockkStatic("android.net.Uri")
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun identFlowOneAttempt() = runTest {
        val mockedPinCallback = mockk<(String, String) -> Unit>(relaxed = true)

        val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.RequestPinAndCan(mockedPinCallback))

        every { mockIdCardManager.eidFlow } returns eIdFlow

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)

        canCoordinator.startIdentCanFlow(true)

        Assertions.assertEquals(SubCoordinatorState.Active, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify { mockAppNavigator.navigate(IdentificationCanPinForgottenDestination) }

        canCoordinator.proceedWithThirdAttempt()

        Assertions.assertEquals(IdentificationCanIntroDestination(true).route, destinationSlot.captured.route)

        canCoordinator.finishIntro()

        Assertions.assertEquals(CanInputDestination(false).route, destinationSlot.captured.route)

        canCoordinator.onCanEntered(can)

        verify { mockAppNavigator.navigate(IdentificationCanPinInputDestination) }
        verify(exactly = 0) { mockedPinCallback(any(), any()) }

        canCoordinator.onPinEntered(pin)

        verify(exactly = 1) { mockedPinCallback(pin, can) }

        eIdFlow.value = EidInteractionEvent.RequestCardInsertion

        advanceUntilIdle()

        eIdFlow.value = EidInteractionEvent.AuthenticationSuccessful

        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun identFlowSecondAttempt() = runTest {
        val mockedPinCallback = mockk<(String, String) -> Unit>(relaxed = true)

        val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.RequestPinAndCan(mockedPinCallback))

        every { mockIdCardManager.eidFlow } returns eIdFlow

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        val incorrectCan = "999999"
        canCoordinator.startIdentCanFlow(true)
        advanceUntilIdle()
        canCoordinator.proceedWithThirdAttempt()
        canCoordinator.finishIntro()
        canCoordinator.onCanEntered(incorrectCan)
        canCoordinator.onPinEntered(pin)

        verify(exactly = 1) { mockedPinCallback(pin, incorrectCan) }

        eIdFlow.value =  EidInteractionEvent.RequestPinAndCan(mockedPinCallback)

        advanceUntilIdle()

        verify(exactly = 2) { mockAppNavigator.navigate(IdentificationCanPinForgottenDestination) }
        Assertions.assertEquals(CanInputDestination(true).route, destinationSlot.captured.route)

        canCoordinator.onCanEntered(can)

        advanceUntilIdle()

        verify(exactly = 1) { mockedPinCallback(pin, can) }

        eIdFlow.value = EidInteractionEvent.RequestCardInsertion

        advanceUntilIdle()

        eIdFlow.value = EidInteractionEvent.AuthenticationSuccessful

        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @SealedClassesSource(names = ["RequestPinAndCan"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = EidInteractionEventTypeFactory::class)
    fun initializeIdentFlowWithUnexpectedEidEvent(event: EidInteractionEvent) = runTest {
        every { mockIdCardManager.eidFlow } returns flowOf(event)

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        canCoordinator.startIdentCanFlow(true)

        Assertions.assertEquals(SubCoordinatorState.Active, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify(exactly = 0) { mockAppNavigator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setupFlowOneAttempt() = runTest {
        val mockedPinCallback = mockk<(String, String, String) -> Unit>(relaxed = true)

        val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.RequestCanAndChangedPin(mockedPinCallback))

        every { mockIdCardManager.eidFlow } returns eIdFlow

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)

        canCoordinator.startSetupCanFlow(true, pin, newPin)

        Assertions.assertEquals(SubCoordinatorState.Active, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        Assertions.assertEquals(SetupCanConfirmTransportPinDestination(pin).route, destinationSlot.captured.route)

        canCoordinator.proceedWithThirdAttempt()

        Assertions.assertEquals(SetupCanIntroDestination(true).route, destinationSlot.captured.route)

        canCoordinator.finishIntro()

        Assertions.assertEquals(CanInputDestination(false).route, destinationSlot.captured.route)

        canCoordinator.onCanEntered(can)

        verify { mockAppNavigator.navigate(SetupCanTransportPinDestination) }
        verify(exactly = 0) { mockedPinCallback(any(), any(), any()) }

        canCoordinator.onPinEntered(pin)

        verify(exactly = 1) { mockedPinCallback(pin, can, newPin) }

        eIdFlow.value = EidInteractionEvent.RequestCardInsertion

        advanceUntilIdle()

        eIdFlow.value = EidInteractionEvent.AuthenticationSuccessful

        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setupFlowSecondAttempt() = runTest {
        val mockedPinCallback = mockk<(String, String, String) -> Unit>(relaxed = true)

        val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.RequestCanAndChangedPin(mockedPinCallback))

        every { mockIdCardManager.eidFlow } returns eIdFlow

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        val incorrectCan = "999999"
        canCoordinator.startSetupCanFlow(true, pin, newPin)
        advanceUntilIdle()
        canCoordinator.proceedWithThirdAttempt()
        canCoordinator.finishIntro()
        canCoordinator.onCanEntered(incorrectCan)
        canCoordinator.onPinEntered(pin)

        verify(exactly = 1) { mockedPinCallback(pin, incorrectCan, newPin) }

        eIdFlow.value = EidInteractionEvent.RequestCardInsertion

        advanceUntilIdle()

        eIdFlow.value = EidInteractionEvent.AuthenticationSuccessful

        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Finished, canCoordinator.stateFlow.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @SealedClassesSource(names = ["RequestCanAndChangedPin"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = EidInteractionEventTypeFactory::class)
    fun initializeSetupFlowWithUnexpectedEidEvent(event: EidInteractionEvent) = runTest {
        every { mockIdCardManager.eidFlow } returns flowOf(event)

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        canCoordinator.startSetupCanFlow(true, pin, newPin)

        Assertions.assertEquals(SubCoordinatorState.Active, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify(exactly = 0) { mockAppNavigator.navigate(any()) }
    }

    @Test
    fun resetPin() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)
        canCoordinator.onResetPin()
        verify { mockAppNavigator.navigate(ResetPersonalPinDestination) }
    }

    @Test
    fun confirmPin() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)
        canCoordinator.confirmPinInput()
        verify { mockAppNavigator.navigate(SetupCanAlreadySetupDestination) }
    }

    @Test
    fun finishIntro() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)
        canCoordinator.finishIntro()
        Assertions.assertEquals(CanInputDestination(false).route, destinationSlot.captured.route)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancelCanFlow() = runTest {
        val mockedPinCallback = mockk<(String, String, String) -> Unit>(relaxed = true)

        val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.RequestCanAndChangedPin(mockedPinCallback))

        every { mockIdCardManager.eidFlow } returns eIdFlow

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCoroutineContextProvider)

        canCoordinator.startIdentCanFlow(true)
        canCoordinator.cancelCanFlow()

        Assertions.assertEquals(SubCoordinatorState.Cancelled, canCoordinator.stateFlow.value)
    }

    class EidInteractionEventTypeFactory: DefaultTypeFactory() {
        override fun create(what: KClass<*>): EidInteractionEvent {
            return when (what) {
                EidInteractionEvent.Idle::class -> EidInteractionEvent.Idle
                EidInteractionEvent.Error::class -> EidInteractionEvent.Error(exception = IdCardInteractionException.CardDeactivated)
                EidInteractionEvent.RequestCardInsertion::class -> EidInteractionEvent.RequestCardInsertion
                EidInteractionEvent.CardInteractionComplete::class -> EidInteractionEvent.CardInteractionComplete
                EidInteractionEvent.CardRecognized::class -> EidInteractionEvent.CardRecognized
                EidInteractionEvent.CardRemoved::class -> EidInteractionEvent.CardRemoved
                EidInteractionEvent.RequestCan::class -> EidInteractionEvent.RequestCan { }
                EidInteractionEvent.RequestPin::class -> EidInteractionEvent.RequestPin(null) { }
                EidInteractionEvent.RequestPinAndCan::class -> EidInteractionEvent.RequestPinAndCan { _, _ -> }
                EidInteractionEvent.RequestPuk::class -> EidInteractionEvent.RequestPuk { }
                EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult::class -> EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
                EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect::class -> EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect("")
                EidInteractionEvent.AuthenticationStarted::class -> EidInteractionEvent.AuthenticationStarted
                EidInteractionEvent.RequestAuthenticationRequestConfirmation::class -> EidInteractionEvent.RequestAuthenticationRequestConfirmation(EidAuthenticationRequest("", "", "", "", "", AuthenticationTerms.Text(""), null, mapOf()), { })
                EidInteractionEvent.AuthenticationSuccessful::class -> EidInteractionEvent.AuthenticationSuccessful
                EidInteractionEvent.PinManagementStarted::class -> EidInteractionEvent.PinManagementStarted
                EidInteractionEvent.RequestChangedPin::class -> EidInteractionEvent.RequestChangedPin(null) { _, _ -> }
                EidInteractionEvent.RequestCanAndChangedPin::class -> EidInteractionEvent.RequestCanAndChangedPin {_, _, _ -> }
                else -> throw IllegalArgumentException()
            }
        }
    }
}
