package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.composables.UseIDApp
import de.digitalService.useID.ui.screens.SetupScanViewModel
import de.digitalService.useID.ui.screens.destinations.IdentificationSuccessDestination
import de.digitalService.useID.ui.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
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

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun test() {
        val testErrorState: MutableState<ScanError?> = mutableStateOf(null)
        val testProvider = "testProvider"

        every { mockSetupScanViewModel.errorState } answers { testErrorState.value }
        every { mockSetupScanViewModel.onReEnteredTransportPIN(any(), any()) } answers {
            appCoordinator.navigate(SetupFinishDestination)
        }

        every { mockIdentificationCoordinator.fetchMetadataEventFlow } returns MutableStateFlow(FetchMetadataEvent.Finished)
        every { mockIdentificationCoordinator.startIdentificationProcess("") } answers {
            appCoordinator.navigate(IdentificationSuccessDestination(testProvider, ""))
        }

        every { mockStorageManager.getIsFirstTimeUser() } returns true

        composeTestRule.activity.setContent {
            UseIDApp(appCoordinator)
        }

        val startSetupButton = composeTestRule.activity.getString(R.string.firstTimeUser_intro_no)
        composeTestRule.onNodeWithText(startSetupButton).performClick()

        composeTestRule.onNodeWithText(startSetupButton).assertDoesNotExist()

        val backButtonTag = "backButton"
        composeTestRule.onNodeWithTag(backButtonTag).performClick()

        composeTestRule.onNodeWithText(startSetupButton).performClick()

        val pinLetterAvailableButton = composeTestRule.activity.getString(R.string.firstTimeUser_pinLetter_yes)
        composeTestRule.onNodeWithText(pinLetterAvailableButton).performClick()

        val pinEntryTextFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(5)
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performImeAction()

        val choosePersonalPinButton = composeTestRule.activity.getString(R.string.firstTimeUser_personalPINIntro_continue)
        composeTestRule.onNodeWithText(choosePersonalPinButton).performClick()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(5)

        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag).assertCountEquals(1)
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performImeAction()
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag).assertCountEquals(1)

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("6")
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag).assertCountEquals(2)

        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag)[0].assertIsNotFocused()
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag)[1].assertIsFocused()

        val personalPinError = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_error_mismatch_title)
        composeTestRule.onNodeWithText(personalPinError).assertDoesNotExist()

        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag)[1].performTextInput("111111")

        composeTestRule.onNodeWithText(personalPinError).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag).assertCountEquals(2)

        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag)[0].performTextInput("123456")
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(pinEntryTextFieldTag)[1].performTextInput("123456")

        val setupScanTitle = composeTestRule.activity.getString(R.string.firstTimeUser_scan_title)
        composeTestRule.onNodeWithText(setupScanTitle).assertIsDisplayed()

        testErrorState.value = ScanError.PINSuspended
        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.value!!.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.value!!.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockSetupScanViewModel.onCancel() }
        testErrorState.value = ScanError.IncorrectPIN(2)

        composeTestRule.onNodeWithText(errorDialogTitleText).assertDoesNotExist()
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertDoesNotExist()

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performImeAction()

        verify(exactly = 1) { mockSetupScanViewModel.onReEnteredTransportPIN("12345", any()) }

        val setupSuccessCloseButton = composeTestRule.activity.getString(R.string.firstTimeUser_finish_button)
        composeTestRule.onNodeWithText(setupSuccessCloseButton).performClick()
    }
}
