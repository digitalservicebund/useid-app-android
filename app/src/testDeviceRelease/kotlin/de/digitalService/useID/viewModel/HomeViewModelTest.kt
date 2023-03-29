package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.HomeScreenViewModel
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.AbTestManager
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class HomeViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockAppNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockAbTestManager: AbTestManager

    @BeforeEach
    fun setup() {
        every { mockAbTestManager.isSetupIntroTestVariation.value } returns false
    }

    @Test
    fun onHomeScreenLaunched() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.homeScreenLaunched()

        verify(exactly = 1) { mockAppCoordinator.homeScreenLaunched() }
    }

    @Test
    fun onSetupOnlineId() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.setupOnlineId()

        verify(exactly = 1) { mockAppCoordinator.offerIdSetup(null) }
        verify(exactly = 1) { mockTrackerManager.trackButtonPressed("firstTimeUser", "start") }
    }

    @Test
    fun onOnPrivacyButtonClicked() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.onPrivacyButtonClicked()

        verify(exactly = 1) { mockAppNavigator.navigate(PrivacyScreenDestination) }
    }

    @Test
    fun onOnAccessibilityButtonClicked() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.onAccessibilityButtonClicked()

        verify(exactly = 1) { mockAppNavigator.navigate(AccessibilityScreenDestination) }
    }

    @Test
    fun onOnTermsOfUseButtonClicked() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.onTermsOfUseButtonClicked()

        verify(exactly = 1) { mockAppNavigator.navigate(TermsOfUseScreenDestination) }
    }

    @Test
    fun onOnLicenseButtonClicked() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.onLicenseButtonClicked()

        verify(exactly = 1) { mockAppNavigator.navigate(DependenciesScreenDestination) }
    }

    @Test
    fun onOnImprintButtonClicked() {
        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        viewModel.onImprintButtonClicked()

        verify(exactly = 1) { mockAppNavigator.navigate(ImprintScreenDestination) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun testShowVariant(isVariant: Boolean) {
        every { mockAbTestManager.isSetupIntroTestVariation.value } returns isVariant

        val viewModel = HomeScreenViewModel(mockAppCoordinator, mockAppNavigator, mockTrackerManager, mockAbTestManager)

        Assertions.assertEquals(isVariant, viewModel.showVariation)
    }
}
