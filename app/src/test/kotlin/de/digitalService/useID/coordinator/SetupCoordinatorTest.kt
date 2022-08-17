package de.digitalService.useID.coordinator

import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSecureStorageManager: SecureStorageManager

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.startSetupIDCard()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPINLetterDestination) }
    }

    @Test
    fun setupWithPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.setupWithPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupTransportPINDestination) }
    }

    @Test
    fun setupWithoutPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.setupWithoutPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupResetPersonalPINDestination) }
    }

    @Test
    fun onTransportPINEntered() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onTransportPINEntered()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }
    }

    @Test
    fun onPersonalPINIntroFinished() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onPersonalPINIntroFinished()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINDestination) }
    }

    @Test
    fun onPersonalPINEntered() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onPersonalPINEntered()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupScanDestination) }
    }

    @Test
    fun onSettingPINSucceeded() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onSettingPINSucceeded()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupFinishDestination) }
    }

    @Test
    fun onSetupFinished_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(any()) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()
        setupCoordinator.onSetupFinished()

        verify(exactly = 2) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 2) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)

        setupCoordinator.onSkipSetup()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
    }

    @Test
    fun onSkipSetup_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()
        setupCoordinator.onSkipSetup()

        verify(exactly = 2) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun cancelSetup() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockSecureStorageManager)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.cancelSetup()

        verify(exactly = 1) { mockSecureStorageManager.clearStorage() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(testUrl) }

        setupCoordinator.cancelSetup()

        verify(exactly = 2) { mockAppCoordinator.popToRoot() }
    }
}
