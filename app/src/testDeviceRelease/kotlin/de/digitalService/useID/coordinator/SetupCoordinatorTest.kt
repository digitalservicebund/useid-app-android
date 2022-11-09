package de.digitalService.useID.coordinator

import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    private val destinationSlot = slot<Direction>()
    private val testPin = "testPin"

    @BeforeEach
    fun beforeEach() {
        every { mockAppCoordinator.navigate(capture(destinationSlot)) } returns Unit
    }

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.startSetupIDCard()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPINLetterDestination) }
    }

    @Test
    fun setupWithPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.setupWithPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(SetupTransportPINDestination(null).route, navigationParameter.route)
    }

    @Test
    fun setupWithoutPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.setupWithoutPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupResetPersonalPINDestination) }
    }

    @Test
    fun onTransportPINEntered() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        Assertions.assertNull(setupCoordinator.transportPin)

        setupCoordinator.onTransportPINEntered(testPin)

        Assertions.assertEquals(testPin, setupCoordinator.transportPin)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }
    }

    @Test
    fun onPersonalPINIntroFinished() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.onPersonalPINIntroFinished()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinInputDestination) }
    }

    @Test
    fun onPersonalPINInsert() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        Assertions.assertNull(setupCoordinator.personalPin)

        setupCoordinator.onPersonalPinInput(testPin)

        Assertions.assertEquals(testPin, setupCoordinator.personalPin)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }
    }

    @Test
    fun confirmPersonalPin() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        Assertions.assertNull(setupCoordinator.personalPin)

        setupCoordinator.confirmPersonalPin(testPin)

        Assertions.assertNull(setupCoordinator.personalPin)
        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }
        verify(exactly = 0) { mockAppCoordinator.startNFCTagHandling() }
    }

    @Test
    fun onPersonalPINInputAndConfirm() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        Assertions.assertNull(setupCoordinator.personalPin)

        setupCoordinator.onPersonalPinInput(testPin)
        setupCoordinator.confirmPersonalPin(testPin)

        Assertions.assertEquals(testPin, setupCoordinator.personalPin)
        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
    }

    @Test
    fun onPersonalPinErrorTryAgain() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        Assertions.assertNull(setupCoordinator.personalPin)

        setupCoordinator.onPersonalPinErrorTryAgain()

        Assertions.assertNull(setupCoordinator.personalPin)
        verify(exactly = 1) { mockAppCoordinator.pop() }
    }

    @Test
    fun onSettingPINSucceeded() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.onSettingPINSucceeded()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupFinishDestination) }
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.stopNFCTagHandling() }
    }

    @Test
    fun onSetupFinished_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(any()) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()

        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()
        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)

        setupCoordinator.onSkipSetup()

        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
    }

    @Test
    fun onSkipSetup_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()

        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()
        setupCoordinator.onSkipSetup()

        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun cancelSetup() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.cancelSetup()

        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
        verify(exactly = 1) { mockAppCoordinator.stopNFCTagHandling() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(testUrl) }

        setupCoordinator.cancelSetup()

        verify(exactly = 2) { mockAppCoordinator.popToRoot() }
        verify(exactly = 2) { mockAppCoordinator.stopNFCTagHandling() }
    }

    @Test
    fun hasToken() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator)
        val testUrl = "tokenUrl"

        Assertions.assertFalse(setupCoordinator.identificationPending())

        setupCoordinator.setTCTokenURL(testUrl)

        Assertions.assertTrue(setupCoordinator.identificationPending())
    }
}
