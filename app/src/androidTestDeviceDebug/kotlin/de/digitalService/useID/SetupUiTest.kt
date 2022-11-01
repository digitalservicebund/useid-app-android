package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SetupUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @BindValue
    val mockSetupScanViewModel: SetupScanViewModel = mockk(relaxed = true)

    @BindValue
    val mockIdentificationCoordinator: IdentificationCoordinator = mockk(relaxed = true)

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true)

    @BindValue
    val mockTrackerManager: TrackerManager = mockk(relaxed = true)

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun test() {
        val testErrorState: MutableState<ScanError.IncorrectPIN?> = mutableStateOf(null)

        every { mockSetupScanViewModel.errorState } answers { testErrorState.value }
        every { mockSetupScanViewModel.onReEnteredTransportPIN(any(), any()) } answers {
            appCoordinator.navigate(SetupFinishDestination)
        }

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Finished)

        every { mockStorageManager.getIsFirstTimeUser() } returns true

        composeTestRule.activity.setContent {
            UseIDApp(appCoordinator, mockTrackerManager)
        }

        val startSetupButton = composeTestRule.activity.getString(R.string.firstTimeUser_intro_startSetup)
        composeTestRule.onNodeWithText(startSetupButton).performClick()

        val pinLetterAvailableButton = composeTestRule.activity.getString(R.string.firstTimeUser_pinLetter_letterPresent)
        composeTestRule.onNodeWithText(pinLetterAvailableButton).performClick()

        val pinEntryFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performTextInput("12345")
        composeTestRule.onAllNodesWithTag("PINEntry").assertCountEquals(5)
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performImeAction()

        val choosePersonalPinButton = composeTestRule.activity.getString(R.string.firstTimeUser_personalPINIntro_continue)
        composeTestRule.onNodeWithText(choosePersonalPinButton).performClick()

        composeTestRule.onNodeWithTag(pinEntryFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performTextInput("12345")
        composeTestRule.onAllNodesWithTag("Obfuscation").assertCountEquals(5)

        composeTestRule.onAllNodesWithTag(pinEntryFieldTag).assertCountEquals(1)
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performImeAction()
        composeTestRule.onAllNodesWithTag(pinEntryFieldTag).assertCountEquals(1)

        composeTestRule.onNodeWithTag(pinEntryFieldTag).performTextInput("6")
        composeTestRule.onAllNodesWithTag(pinEntryFieldTag).assertCountEquals(2)

        composeTestRule.onAllNodesWithTag(pinEntryFieldTag)[0].assertIsNotFocused()
        composeTestRule.onAllNodesWithTag(pinEntryFieldTag)[1].assertIsFocused()

        val personalPinError = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_error_mismatch_title)
        composeTestRule.onNodeWithText(personalPinError).assertDoesNotExist()

        composeTestRule.onAllNodesWithTag(pinEntryFieldTag)[1].performTextInput("111111")

        composeTestRule.onNodeWithText(personalPinError).performScrollTo()
        composeTestRule.onNodeWithText(personalPinError).assertIsDisplayed()

        composeTestRule.onAllNodesWithTag(pinEntryFieldTag).assertCountEquals(1)

        composeTestRule.onAllNodesWithTag(pinEntryFieldTag)[0].performTextInput("123456")
        composeTestRule.onAllNodesWithTag(pinEntryFieldTag).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(pinEntryFieldTag)[1].performTextInput("123456")

        val setupScanTitle = composeTestRule.activity.getString(R.string.firstTimeUser_scan_title)
        composeTestRule.onNodeWithText(setupScanTitle).assertIsDisplayed()

        testErrorState.value = ScanError.IncorrectPIN(2)
        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()
        
        composeTestRule.onNodeWithTag(pinEntryFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performTextInput("12345")
        composeTestRule.onNodeWithTag(pinEntryFieldTag).performImeAction()

        verify(exactly = 1) { mockSetupScanViewModel.onReEnteredTransportPIN("12345", any()) }

        val setupSuccessCloseButton = composeTestRule.activity.getString(R.string.firstTimeUser_done_close)
        composeTestRule.onNodeWithText(setupSuccessCloseButton).performClick()
    }
}
