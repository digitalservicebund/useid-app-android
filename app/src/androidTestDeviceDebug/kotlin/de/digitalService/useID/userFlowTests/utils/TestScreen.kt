package de.digitalService.useID.userFlowTests.setupFlows

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.IdCardAttribute
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.userFlowTests.utils.TestElement
import de.digitalService.useID.util.*

sealed class TestScreen {
    val progressIndicatorTag = "ProgressIndicator"

    abstract val expectedElements: List<TestElement>
    abstract val unexpectedElements: List<TestElement>

    abstract val testRule: ComposeTestRule

    fun assertIsDisplayed() {
        expectedElements.forEach { element ->
            element.assertIsDisplayed()
        }

        unexpectedElements.forEach { element ->
            element.assertIsNotDisplayed()
        }
    }

    data class Home(override val testRule: ComposeTestRule) : TestScreen() {

        private val titleImage = TestElement.Tag(testRule, R.drawable.abstract_widget_phone.toString())
        private val headerTitle = TestElement.Text(testRule, resourceId = R.string.home_header_title)
        private val headerBody = TestElement.Text(testRule, resourceId = R.string.home_header_body)

        private val title = TestElement.Text(testRule, resourceId = R.string.home_more_title)
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
        val setupIdBtn = TestElement.Text(testRule, resourceId = R.string.home_startSetup)

        val privacyBtn = TestElement.Text(testRule, resourceId = R.string.home_more_privacy)
        val licensesBtn = TestElement.Text(testRule, resourceId = R.string.home_more_licenses)
        val accessibilityBtn = TestElement.Text(testRule, resourceId = R.string.home_more_accessibilityStatement)
        val termsAndConditionsBtn = TestElement.Text(testRule, resourceId = R.string.home_more_terms)
        val imprintBtn = TestElement.Text(testRule, resourceId = R.string.home_more_imprint)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(
                    titleImage, headerTitle, headerBody, title, idsImage, setupIdBtn,
                    privacyBtn, licensesBtn, accessibilityBtn, termsAndConditionsBtn, imprintBtn
                )
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                )
            }
    }

    data class Scan(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = true
        fun setBackAllowed(value: Boolean) : Scan {
            backAllowed = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : Scan {
            identPending = value
            return this
        }

        private var progress = false
        fun setProgress(value: Boolean) : Scan {
            progress = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_scan_title_android)
        private val body = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_scan_body)
        private val progressIndicator = TestElement.Tag(testRule, progressIndicatorTag)

        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val navigationConfirmDialog:  TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        val nfcHelpBtn = TestElement.Text(testRule, resourceId = R.string.scan_helpNFC)
        val scanHelpBtn = TestElement.Text(testRule, resourceId = R.string.scan_helpScanning)

        val nfcDialog = TestElement.StandardDialog(
            testRule,
            dismissBtnId = R.string.scanError_close
        )

        val helpDialog = TestElement.StandardDialog(
            testRule,
            dismissBtnId = R.string.scanError_close
        )

        // TODO: As of now it is unclear whether or not there should be different body descriptions
        // for the setup scan and ident scan screen. Until this is clearified, we ignore the body
        override val expectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(title, /*body,*/ nfcHelpBtn, scanHelpBtn)
                    .plus(listOf(progressIndicator).takeIf { progress } ?: listOf())
                    .plus(listOf(back).takeIf { backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { !backAllowed } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog, nfcDialog, helpDialog)
                    .plus(listOf<TestElement>(progressIndicator).takeIf { !progress } ?: listOf())
                    .plus(listOf(back).takeIf { !backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { backAllowed } ?: listOf())
            }
    }

    data class ResetPersonalPin(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_missingPINLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_missingPINLetter_body) TODO: reenable when markdown is matchable in UI tests
        private val pinLetterImage = TestElement.Tag(testRule, R.drawable.ic_illustration_pin_letter.toString())

        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, pinLetterImage, back)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
            }
    }

    // NFC SCREENS

    data class NoNfc(override val testRule: ComposeTestRule) : TestScreen() {

        private val titleImage = TestElement.Tag(testRule, "NoNfcImage")
        private val title = TestElement.Text(testRule, resourceId = R.string.noNfc_info_title)
        private val body = TestElement.Text(testRule, resourceId = R.string.noNfc_info_body)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(
                    titleImage, title, body
                )
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                )
            }
    }

    data class NfcDeactivated(override val testRule: ComposeTestRule) : TestScreen() {

        private val titleImage = TestElement.Tag(testRule, "NfcDeactivatedImage")
        private val title = TestElement.Text(testRule, resourceId = R.string.nfcDeactivated_info_title)
        private val body = TestElement.Text(testRule, resourceId = R.string.nfcDeactivated_info_body)

        val adaptSettingsBtn = TestElement.Text(testRule, resourceId = R.string.ndcDeactivated_openSettings_button)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(
                    titleImage, title, body, adaptSettingsBtn
                )
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                )
            }
    }

    // CAN SCREENS

    data class CanIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = false
        fun setBackAllowed(value: Boolean) : CanIntro {
            backAllowed = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : CanIntro {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.identification_can_intro_title)
        //        private val body = TestElement.Text(testRule, resourceId = R.string.identification_can_intro_body) TODO: reenable when markdown is matchable in UI tests
        val enterCanNowBtn = TestElement.Text(testRule, resourceId = R.string.identification_can_intro_continue)
        private val canImage = TestElement.Tag(testRule, R.drawable.illustration_id_can.toString())

        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, canImage, enterCanNowBtn)
                    .plus(listOf(back).takeIf { backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { !backAllowed } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog)
                    .plus(listOf(back).takeIf { !backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { backAllowed } ?: listOf())
            }
    }

    data class CanInput(override val testRule: ComposeTestRule) : TestScreen() {

        private var retry = false
        fun setRetry(value: Boolean) : CanInput {
            retry = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.identification_can_input_title)
        private val body = TestElement.Text(testRule, resourceId = R.string.identification_can_input_body)
        private val errorMessage = TestElement.Text(testRule, resourceId = R.string.identification_can_incorrectInput_error_incorrect_body)
        private val retryMessage = TestElement.Text(testRule, resourceId = R.string.identification_personalPIN_error_tryAgain)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val canEntryField = TestElement.Can(testRule)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, body, back, canEntryField)
                    .plus(listOf(retryMessage, errorMessage).takeIf { retry } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
                    .plus(listOf(retryMessage, errorMessage).takeIf { !retry } ?: listOf())
            }
    }

    // SETUP SCREENS

    data class SetupIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_intro_title)
//        val body = TestElement.Text(R.string.firstTimeUser_intro_body) TODO: reenable when markdown is matchable in UI tests
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val setupIdBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_intro_startSetup)
        val alreadySetupBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_intro_skipSetup)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(
                    title, idsImage, cancel, setupIdBtn, alreadySetupBtn
                )
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name)
                )
            }
    }

    data class SetupPinLetter(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_pinLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_pinLetter_body) TODO: reenable when markdown is matchable in UI tests
        private val pinLetterImage = TestElement.Tag(testRule, R.drawable.pin_letter.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val letterPresentBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_pinLetter_letterPresent)
        val noLetterBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_pinLetter_requestLetter)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(
                    title, pinLetterImage, back, letterPresentBtn, noLetterBtn
                )
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name)
                )
            }
    }

    data class SetupTransportPin(override val testRule: ComposeTestRule) : TestScreen() {

        private var attemptsLeft = 3
        fun setAttemptsLeft(value: Int) : SetupTransportPin {
            attemptsLeft = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupTransportPin {
            identPending = value
            return this
        }

        private val titleSecondAttempt = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_incorrectTransportPIN_title)
        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_transportPIN_title)

        private val body = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_transportPIN_body)
        private val oneAttemptLeftMessage = TestElement.Text(testRule, resourceId = R.plurals.firstTimeUser_transportPIN_remainingAttempts, quantity = 1)

        val transportPinField = TestElement.TransportPin(testRule)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(body, transportPinField)
                    .plus(listOf(oneAttemptLeftMessage).takeIf { attemptsLeft == 1 } ?: listOf())
                    .plus(listOf(cancel, titleSecondAttempt).takeIf { attemptsLeft == 2 } ?: listOf())
                    .plus(listOf(back, title).takeIf { attemptsLeft != 2 } ?: listOf())

            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog)
                    .plus(listOf(oneAttemptLeftMessage).takeIf { attemptsLeft != 1 } ?: listOf())
                    .plus(listOf(titleSecondAttempt, cancel).takeIf { attemptsLeft != 2 } ?: listOf())
                    .plus(listOf(back, title).takeIf { attemptsLeft == 2 } ?: listOf())
            }
    }

    data class SetupPersonalPinIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPINIntro_title)
        private val card = TestElement.BundCard(
            testRule,
            titleResId = R.string.firstTimeUser_personalPINIntro_info_title,
            bodyResId = R.string.firstTimeUser_personalPINIntro_info_body,
            iconTag = Icons.Filled.Info.name
        )
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val continueBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPINIntro_continue)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, card, idsImage, back, continueBtn)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
            }
    }

    data class SetupPersonalPinInput(override val testRule: ComposeTestRule) : TestScreen() {
        // Components of this screen
        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPIN_title)
        private val body = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPIN_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val personalPinField = TestElement.PersonalPin(testRule)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, body, personalPinField, back)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
            }
    }

    data class SetupPersonalPinConfirm(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPIN_confirmation_title)
        private val body = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_personalPIN_confirmation_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val personalPinField = TestElement.PersonalPin(testRule)
        val pinsDontMatchDialog = TestElement.StandardDialog(
            testRule,
            dismissBtnId = R.string.identification_fetchMetadataError_retry
        )

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, body, personalPinField, back)
            }

        override val unexpectedElements: List<TestElement>
            get()  {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                    pinsDontMatchDialog
                )
            }
    }

    data class SetupFinish(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupFinish {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_done_title)
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val identifyNowBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_done_identify)
        val finishSetupBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_done_close)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, idsImage)
                    .plus(listOf(identifyNowBtn).takeIf { identPending } ?: listOf())
                    .plus(listOf(finishSetupBtn, cancel).takeIf { !identPending } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Back.name))
                    .plus(listOf(identifyNowBtn).takeIf { !identPending } ?: listOf())
                    .plus(listOf(finishSetupBtn, cancel).takeIf { identPending } ?: listOf())
            }
    }

    data class SetupCanConfirmTransportPin(override val testRule: ComposeTestRule) : TestScreen() {

        private var transportPin = ""
        fun setTransportPin(value: String) : SetupCanConfirmTransportPin {
            transportPin = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupCanConfirmTransportPin {
            identPending = value
            return this
        }

        private val title: TestElement
            get() {
                return TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_confirmTransportPIN_title, formatArg = transportPin)
            }

//        private val body: TestElement TODO: reenable when markdown is matchable in UI tests
//            get() {
//                return TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_confirmTransportPIN_body, transportPin)
//            }
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val inputCorrectBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_confirmTransportPIN_confirmInput)
        val retryInputBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_confirmTransportPIN_incorrectInput)
        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel, inputCorrectBtn, retryInputBtn)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    navigationConfirmDialog
                )
            }
    }

    data class SetupCanAlreadySetup(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupCanAlreadySetup {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_alreadySetup_title)

        // TODO: Use when markdwon is matchable
        private val bodyIdent = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_alreadySetup_body_ident)
        private val bodyNoIdent = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_alreadySetup_body_setup)

        val personalPinNotAvailableBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_can_alreadySetup_personalPINNotAvailable)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)

        val finishSetupBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_done_close)
        val identifyNowBtn = TestElement.Text(testRule, resourceId = R.string.firstTimeUser_done_identify)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, back, personalPinNotAvailableBtn)
                    .plus(listOf(/*bodyIdent,*/ identifyNowBtn).takeIf { identPending } ?: listOf())
                    .plus(listOf(/*bodyNoIdent,*/ finishSetupBtn).takeIf { !identPending } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
                    .plus(listOf(/*bodyIdent,*/ identifyNowBtn).takeIf { !identPending } ?: listOf())
                    .plus(listOf(/*bodyNoIdent,*/ finishSetupBtn).takeIf { identPending } ?: listOf())
            }
    }

    // IDENTIFICATION SCREENS

    data class IdentificationFetchMetaData(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = false
        fun setBackAllowed(value: Boolean) : IdentificationFetchMetaData {
            backAllowed = value
            return this
        }

        private val progressIndicator = TestElement.Tag(testRule, progressIndicatorTag)
        private val loadingLabel = TestElement.Text(testRule, resourceId = R.string.identification_fetchMetadata_pleaseWait)

        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, true)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(progressIndicator, loadingLabel)
                    .plus(listOf(back).takeIf { backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { !backAllowed } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog)
                    .plus(listOf(back).takeIf { !backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { backAllowed } ?: listOf())
            }
    }

    data class IdentificationAttributeConsent(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = false
        fun setBackAllowed(value: Boolean) : IdentificationAttributeConsent {
            backAllowed = value
            return this
        }

        object RequestData {
            val issuer = "issuer"
            val issuerURL = "issueURL"
            val subject = "subject"
            val subjectURL = "subjectURL"
            val validity = "validity"
            val authenticationTerms = "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            val transactionInfo = "transactionInfo"
            val readAttributes = IdCardAttribute.values().associateWith { true }
        }

        private fun attributeDescriptionID(attribute: IdCardAttribute): Int = when (attribute) {
            IdCardAttribute.DG01 -> R.string.cardAttribute_dg01
            IdCardAttribute.DG02 -> R.string.cardAttribute_dg02
            IdCardAttribute.DG03 -> R.string.cardAttribute_dg03
            IdCardAttribute.DG04 -> R.string.cardAttribute_dg04
            IdCardAttribute.DG05 -> R.string.cardAttribute_dg05
            IdCardAttribute.DG06 -> R.string.cardAttribute_dg06
            IdCardAttribute.DG07 -> R.string.cardAttribute_dg07
            IdCardAttribute.DG08 -> R.string.cardAttribute_dg08
            IdCardAttribute.DG09 -> R.string.cardAttribute_dg09
            IdCardAttribute.DG10 -> R.string.cardAttribute_dg10
            IdCardAttribute.DG13 -> R.string.cardAttribute_dg13
            IdCardAttribute.DG17 -> R.string.cardAttribute_dg17
            IdCardAttribute.DG19 -> R.string.cardAttribute_dg19
            IdCardAttribute.RESTRICTED_IDENTIFICATION -> R.string.cardAttribute_restrictedIdentification
            IdCardAttribute.AGE_VERIFICATION -> R.string.cardAttribute_ageVerification
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.identification_attributeConsent_title, formatArg = RequestData.subject)
        private val body = TestElement.Text(testRule, resourceId = R.string.identification_attributeConsent_body)
        private val readAttributes = IdCardAttribute.values().map {
            TestElement.Text(testRule, text = "\u2022 ${testRule.activity.getString(attributeDescriptionID(it))}")
        }

        val moreInformationBtn = TestElement.Text(testRule, resourceId = R.string.identification_attributeConsent_button_additionalInformation)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val continueBtn = TestElement.Text(testRule, resourceId = R.string.identification_attributeConsent_continue)
        // TODO: Find a way to test against the actual strings, currently the matcher finds multiple matches
        // if the actual strings are used, because some are already on the "parent" page
        val infoDialog = TestElement.Group(testRule,
            listOf(
                TestElement.Tag(testRule, "subjectTitle"),
                TestElement.Tag(testRule, "providerInfoTitle"),
                TestElement.Text(testRule, resourceId = R.string.identification_attributeConsentInfo_provider),
                TestElement.Tag(testRule, "subjectName"),
                TestElement.Tag(testRule, "subjectURL"),
                TestElement.Text(testRule, resourceId = R.string.identification_attributeConsentInfo_issuer),
                TestElement.Tag(testRule, "issuerName"),
                TestElement.Tag(testRule, "issuerURL"),
                TestElement.Tag(testRule, "providerInfoSubtitle"),
                TestElement.Tag(testRule, "terms"),
            )
        )
        val infoDialogCloseBtn = TestElement.Tag(testRule, "infoDialogCancel")

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, true)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(title, body, continueBtn)
                    .plus(readAttributes)
                    .plus(listOf(back).takeIf { backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { !backAllowed } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog, infoDialog)
                    .plus(listOf(back).takeIf { !backAllowed } ?: listOf())
                    .plus(listOf(cancel).takeIf { backAllowed } ?: listOf())
            }
    }

    data class IdentificationPersonalPin(override val testRule: ComposeTestRule) : TestScreen() {

        private var attemptsLeft = 3
        fun setAttemptsLeft(value: Int) : IdentificationPersonalPin {
            attemptsLeft = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.identification_personalPIN_title)
        val personalPinField = TestElement.PersonalPin(testRule)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        private val twoAttemptsLeftMessages = TestElement.Group(testRule, listOf(
            TestElement.Text(testRule, resourceId = R.string.identification_personalPIN_error_incorrectPIN),
            TestElement.Text(testRule, resourceId = R.string.identification_personalPIN_error_tryAgain),
            TestElement.Text(testRule, resourceId = R.plurals.firstTimeUser_transportPIN_remainingAttempts, formatArg = "2", quantity = 2)
        ))

        private val oneAttemptLeftMessage = TestElement.Text(testRule, resourceId = R.plurals.firstTimeUser_transportPIN_remainingAttempts, formatArg = "1", quantity = 1)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, true)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, personalPinField)
                    .plus(listOf(back).takeIf { attemptsLeft == 3 } ?: listOf())
                    .plus(listOf(cancel, twoAttemptsLeftMessages ).takeIf { attemptsLeft == 2 } ?: listOf())
                    .plus(listOf(back, oneAttemptLeftMessage).takeIf { attemptsLeft == 1 } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(navigationConfirmDialog)
                    .plus(listOf(back).takeIf { attemptsLeft == 2 } ?: listOf())
                    .plus(listOf(cancel, twoAttemptsLeftMessages ).takeIf { attemptsLeft != 2 } ?: listOf())
                    .plus(listOf(oneAttemptLeftMessage).takeIf { attemptsLeft != 1 } ?: listOf())
            }
    }

    data class IdentificationCanPinForgotten(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.identification_can_pinForgotten_title)
//        private val body = TestElement.Text(testRule, resourceId = R.string.identification_can_pinForgotten_body) // TODO: reenable when markdown is matchable in UI tests
        private val idCardImage = TestElement.Tag(testRule, R.drawable.illustration_id_confused.toString())

        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val iWantANewPinBtn = TestElement.Text(testRule, resourceId = R.string.identification_can_pinForgotten_orderNewPin)
        val tryAgainBtn = TestElement.Text(testRule, resourceId = R.string.identification_can_pinForgotten_retry)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, true)
            }

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel, iWantANewPinBtn, tryAgainBtn)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    navigationConfirmDialog
                )
            }
    }

    // ERROR SCREENS

    data class ErrorCardDeactivated(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.scanError_cardDeactivated_title)
        //        val body = TestElement.Text(R.string.scanError_cardDeactivated_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val closeBtn = TestElement.Text(testRule, resourceId = R.string.scanError_close)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel, closeBtn)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Back.name))
            }
    }

    data class ErrorCardUnreadable(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : ErrorCardUnreadable {
            identPending = value
            return this
        }

        private var redirectUrlPresent = false
        fun setRedirectUrlPresent(value: Boolean) : ErrorCardUnreadable {
            redirectUrlPresent = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.scanError_cardUnreadable_title)
        //        val body = TestElement.Text(R.string.scanError_cardUnreadable_body) TODO: reenable when markdown is matchable in UI tests
        private val errorCard = TestElement.BundCard(
            testRule,
            titleResId = R.string.scanError_box_title,
            bodyResId = R.string.scanError_box_body,
            iconTag = Icons.Filled.Dangerous.name
        )

        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val backToServiceProviderBtn = TestElement.Text(testRule, resourceId = R.string.scanError_redirect)
        val closeBtn = TestElement.Text(testRule, resourceId = R.string.scanError_close)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel)
                    .plus(listOf(errorCard).takeIf { identPending } ?: listOf())
                    .plus(listOf(backToServiceProviderBtn).takeIf { redirectUrlPresent } ?: listOf())
                    .plus(listOf(closeBtn).takeIf { !redirectUrlPresent } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf<TestElement>(TestElement.Tag(testRule, NavigationIcon.Back.name))
                    .plus(listOf(errorCard).takeIf { !identPending } ?: listOf())
                    .plus(listOf(backToServiceProviderBtn).takeIf { !redirectUrlPresent } ?: listOf())
                    .plus(listOf(closeBtn).takeIf { redirectUrlPresent } ?: listOf())
            }
    }

    data class ErrorGenericError(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : ErrorGenericError {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, resourceId = R.string.scanError_unknown_title)
        //        val body = TestElement.Text(R.string.scanError_unknown_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val confirmationDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        val tryAgainBtn = TestElement.Text(testRule, resourceId = R.string.identification_fetchMetadataError_retry)
        val closeBtn = TestElement.Text(testRule, resourceId = R.string.scanError_close)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel)
                    .plus(listOf(tryAgainBtn).takeIf { identPending } ?: listOf())
                    .plus(listOf(closeBtn).takeIf { !identPending } ?: listOf())
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Back.name), confirmationDialog)
                    .plus(listOf(tryAgainBtn).takeIf { !identPending } ?: listOf())
                    .plus(listOf(closeBtn).takeIf { identPending } ?: listOf())
            }
    }

    data class ErrorCardBlocked(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, resourceId = R.string.scanError_cardBlocked_title)
        //        val body = TestElement.Text(R.string.scanError_cardBlocked_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val closeBtn = TestElement.Text(testRule, resourceId = R.string.scanError_close)

        override val expectedElements: List<TestElement>
            get() {
                return listOf(title, cancel, closeBtn)
            }

        override val unexpectedElements: List<TestElement>
            get() {
                return listOf(TestElement.Tag(testRule, NavigationIcon.Back.name))
            }
    }
}
