package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.UseIDApp
import de.digitalService.useID.ui.composables.screens.SetupScanViewModel
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.composables.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadataViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class IdentificationUiTest {


    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @BindValue
    val mockSetupScanViewModel: SetupScanViewModel = mockk(relaxed = true)

    @BindValue
    val mockIdentificationFetchMetadataViewModel: IdentificationFetchMetadataViewModel = mockk(relaxed = true)

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun test() {
        val testErrorState: MutableState<ScanError?> = mutableStateOf(null)

        every { mockSetupScanViewModel.errorState } answers { testErrorState.value }
        every { mockSetupScanViewModel.onReEnteredTransportPIN(any(), any()) } answers { appCoordinator.navigate(SetupFinishDestination) }

        every { mockIdentificationFetchMetadataViewModel.fetchMetadata() } answers {
            appCoordinator.navigate(
                IdentificationAttributeConsentDestination(
                    EIDAuthenticationRequest(
                        "ISSUER", "ISSUER_URL", "SUBJECT", "SUBJECT_URL", "VALIDITY", AuthenticationTerms.Text("TEXT"), null, mapOf(
                            IDCardAttribute.DG01 to true,
                            IDCardAttribute.DG02 to true,
                            IDCardAttribute.DG03 to false,
                            IDCardAttribute.DG04 to true,
                            IDCardAttribute.DG05 to false,
                        )
                    )
                )
            )
        }

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

        testErrorState.value = ScanError.IncorrectPIN(2)

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performImeAction()

        verify(exactly = 1) { mockSetupScanViewModel.onReEnteredTransportPIN("12345", any()) }

        composeTestRule.onNodeWithText("Schließen").performClick()

        val attributeText1 = composeTestRule.activity.getString(R.string.idCardAttribute_DG02)
        composeTestRule.onNodeWithText(attributeText1, substring = true).assertIsDisplayed()
        val attributeText2 = composeTestRule.activity.getString(R.string.idCardAttribute_DG02)
        composeTestRule.onNodeWithText(attributeText2, substring = true).assertIsDisplayed()
        val attributeText3 = composeTestRule.activity.getString(R.string.idCardAttribute_DG04)
        composeTestRule.onNodeWithText(attributeText3, substring = true).assertIsDisplayed()

        val privateIdentificationButtonText = composeTestRule.activity.getString(R.string.identification_attributeConsent_pinButton)
        composeTestRule.onNodeWithText(privateIdentificationButtonText).performClick()
    }
}
