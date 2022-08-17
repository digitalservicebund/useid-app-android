package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.composables.UseIDApp
import de.digitalService.useID.ui.screens.setup.SetupIntroViewModel
import de.digitalService.useID.ui.screens.SetupScanViewModel
import de.digitalService.useID.ui.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModel
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
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

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Inject
    lateinit var storageManager: StorageManager

    @BindValue
    lateinit var setupIntroViewModel: SetupIntroViewModel

    @BindValue
    val mockSetupScanViewModel: SetupScanViewModel = mockk(relaxed = true)

    @BindValue
    val mockIdentificationFetchMetadataViewModel: IdentificationFetchMetadataViewModel = mockk(relaxed = true)

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true)

    @Before
    fun before() {
        hiltRule.inject()

        val savedStateHandle = SavedStateHandle(mapOf(Pair("tcTokenURL", "https://tokenURL")))
        setupIntroViewModel = SetupIntroViewModel(SetupCoordinator(appCoordinator, mockk(relaxUnitFun = true)), savedStateHandle)
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
                            IDCardAttribute.DG04 to true,
                        )
                    )
                )
            )
        }

        every { mockStorageManager.getIsFirstTimeUser() } returns true

        composeTestRule.activity.setContent {
            UseIDApp(appCoordinator)
        }

        val skipSetupButton = composeTestRule.activity.getString(R.string.firstTimeUser_intro_yes)
        composeTestRule.onNodeWithText(skipSetupButton).performClick()

        composeTestRule.onNodeWithText(skipSetupButton).assertDoesNotExist()

        val dg01Text = composeTestRule.activity.getString(R.string.idCardAttribute_DG01)
        composeTestRule.onNodeWithText(dg01Text, substring = true).assertIsDisplayed()
        val dg02Text = composeTestRule.activity.getString(R.string.idCardAttribute_DG02)
        composeTestRule.onNodeWithText(dg02Text, substring = true).assertIsDisplayed()
        val dg04Text = composeTestRule.activity.getString(R.string.idCardAttribute_DG04)
        composeTestRule.onNodeWithText(dg04Text, substring = true).assertIsDisplayed()

        val privateIdentificationButtonText = composeTestRule.activity.getString(R.string.identification_attributeConsent_pinButton)
        composeTestRule.onNodeWithText(privateIdentificationButtonText).performClick()
    }
}
