package de.digitalService.useID.userFlowTests.setupFlows

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.userFlowTests.utils.TestElement
import de.digitalService.useID.util.*

sealed class TestScreen {
    val progressIndicatorTag = "ProgressIndicator"

    abstract val expectedElements: Array<TestElement>
    abstract val unexpectedElements: Array<TestElement>

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
        private val headerTitle = TestElement.Text(testRule, R.string.home_header_title)
        private val headerBody = TestElement.Text(testRule, R.string.home_header_body)

        private val title = TestElement.Text(testRule, R.string.home_more_title)
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
        val setupIdBtn = TestElement.Text(testRule, R.string.home_startSetup)

        val privacyBtn = TestElement.Text(testRule, R.string.home_more_privacy)
        val licensesBtn = TestElement.Text(testRule, R.string.home_more_licenses)
        val accessibilityBtn = TestElement.Text(testRule, R.string.home_more_accessibilityStatement)
        val termsAndConditionsBtn = TestElement.Text(testRule, R.string.home_more_terms)
        val imprintBtn = TestElement.Text(testRule, R.string.home_more_imprint)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    titleImage, headerTitle, headerBody, title, idsImage, setupIdBtn,
//                                Not displayed as they are outside of the screen area
                    //            privacyBtn, licensesBtn, accessibilityBtn, termsAndConditionsBtn, imprintBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                )
            }
    }

    // SETUP SCREENS

    data class SetupIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_intro_title)
//        val body = TestElement.Text(R.string.firstTimeUser_intro_body) TODO: reenable when markdown is matchable in UI tests
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val setupIdBtn = TestElement.Text(testRule, R.string.firstTimeUser_intro_startSetup)
        val alreadySetupBtn = TestElement.Text(testRule, R.string.firstTimeUser_intro_skipSetup)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, idsImage, cancel, setupIdBtn, alreadySetupBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name)
                )
            }
    }

    data class SetupPinLetter(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_pinLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_pinLetter_body) TODO: reenable when markdown is matchable in UI tests
        private val pinLetterImage = TestElement.Tag(testRule, R.drawable.pin_letter.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val letterPresentBtn = TestElement.Text(testRule, R.string.firstTimeUser_pinLetter_letterPresent)
        val noLetterBtn = TestElement.Text(testRule, R.string.firstTimeUser_pinLetter_requestLetter)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, pinLetterImage, back, letterPresentBtn, noLetterBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
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

        private val titleSecondAttempt = TestElement.Text(testRule, R.string.firstTimeUser_incorrectTransportPIN_title)
        private val title = TestElement.Text(testRule, R.string.firstTimeUser_transportPIN_title)

        private val body = TestElement.Text(testRule, R.string.firstTimeUser_transportPIN_body)
        private val twoAttemptsLeftMessage = TestElement.Text(testRule, R.plurals.firstTimeUser_transportPIN_remainingAttempts, formatArg = "2", quantity = 2)
        private val oneAttemptLeftMessage = TestElement.Text(testRule, R.plurals.firstTimeUser_transportPIN_remainingAttempts, quantity = 1)

        val transportPinField = TestElement.TransportPin(testRule)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(body, transportPinField)
                    .plus(arrayOf(oneAttemptLeftMessage).takeIf { attemptsLeft == 1 } ?: arrayOf())
                    .plus(arrayOf(cancel, titleSecondAttempt).takeIf { attemptsLeft == 2 } ?: arrayOf())
                    .plus(arrayOf(back, title).takeIf { attemptsLeft != 2 } ?: arrayOf())
                    //.plus(arrayOf(twoAttemptsLeftMessage).takeIf { attemptsLeft == 2 } ?: arrayOf()) TODO: this should be displayed when there are two attemps left
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(navigationConfirmDialog)
                    .plus(arrayOf(oneAttemptLeftMessage).takeIf { attemptsLeft != 1 } ?: arrayOf())
                    .plus(arrayOf(twoAttemptsLeftMessage, titleSecondAttempt, cancel).takeIf { attemptsLeft != 2 } ?: arrayOf())
                    .plus(arrayOf(back, title).takeIf { attemptsLeft == 2 } ?: arrayOf())
            }
    }

    data class SetupPersonalPinIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_title)
        private val card = TestElement.BundCard(
            testRule,
            titleResId = R.string.firstTimeUser_personalPINIntro_info_title,
            bodyResId = R.string.firstTimeUser_personalPINIntro_info_body,
            iconTag = Icons.Filled.Info.name
        )
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val continueBtn = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_continue)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, card, idsImage, back, continueBtn)
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
            }
    }

    data class SetupPersonalPinInput(override val testRule: ComposeTestRule) : TestScreen() {
        // Components of this screen
        private val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_title)
        private val body = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val personalPinField = TestElement.PersonalPin(testRule)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, body, personalPinField, back)
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
            }
    }

    data class SetupPersonalPinConfirm(override val testRule: ComposeTestRule) : TestScreen() {

        private var error = false
        fun setError(value: Boolean) : SetupPersonalPinConfirm {
            error = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_title)
        private val body = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val personalPinField = TestElement.PersonalPin(testRule)
        val pinsDontMatchDialog = TestElement.StandardDialog(
            testRule,
            titleResId = R.string.firstTimeUser_personalPIN_error_mismatch_title,
            dismissBtnId = R.string.identification_fetchMetadataError_retry
        )

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, body, personalPinField, back)
            }

        override val unexpectedElements: Array<TestElement>
            get()  {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                    pinsDontMatchDialog
                )
            }
    }

    data class SetupScan(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = true
        fun setBackAllowed(value: Boolean) : SetupScan {
            backAllowed = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupScan {
            identPending = value
            return this
        }

        private var progress = false
        fun setProgress(value: Boolean) : SetupScan {
            progress = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_scan_title)
        private val body = TestElement.Text(testRule, R.string.firstTimeUser_scan_body)
        private val progressIndicator = TestElement.Tag(testRule, progressIndicatorTag)

        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val navigationConfirmaDialog:  TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        val nfcHelpBtn = TestElement.Text(testRule, R.string.scan_helpNFC)
        val scanHelpBtn = TestElement.Text(testRule, R.string.scan_helpScanning)

        val nfcDialog = TestElement.StandardDialog(
            testRule,
            titleResId = R.string.helpNFC_title,
            dismissBtnId = R.string.scanError_close
        )

        val helpDialog = TestElement.StandardDialog(
            testRule,
            titleResId = R.string.scanError_cardUnreadable_title,
            dismissBtnId = R.string.scanError_close
        )

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(title, body, nfcHelpBtn, scanHelpBtn)
                    .plus(arrayOf(progressIndicator).takeIf { progress } ?: arrayOf())
                    .plus(arrayOf(back).takeIf { backAllowed } ?: arrayOf())
                    .plus(arrayOf(cancel).takeIf { !backAllowed } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(navigationConfirmaDialog, nfcDialog, helpDialog)
                    .plus(arrayOf<TestElement>(progressIndicator).takeIf { !progress } ?: arrayOf())
                    .plus(arrayOf(back).takeIf { !backAllowed } ?: arrayOf())
                    .plus(arrayOf(cancel).takeIf { backAllowed } ?: arrayOf())
            }
    }

    data class SetupFinish(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupFinish {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_done_title)
        private val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val identifyNowBtn = TestElement.Text(testRule, R.string.firstTimeUser_done_identify)
        val finishSetupBtn = TestElement.Text(testRule, R.string.firstTimeUser_done_close)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, idsImage, cancel, finishSetupBtn)
                    .plus(arrayOf(identifyNowBtn).takeIf { identPending } ?: arrayOf())
                    .plus(arrayOf(finishSetupBtn).takeIf { !identPending } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(TestElement.Tag(testRule, NavigationIcon.Back.name), navigationConfirmDialog)
                    .plus(arrayOf(identifyNowBtn).takeIf { !identPending } ?: arrayOf())
                    .plus(arrayOf(finishSetupBtn).takeIf { identPending } ?: arrayOf())
            }
    }

    data class ResetPersonalPin(override val testRule: ComposeTestRule) : TestScreen() {

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_missingPINLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_missingPINLetter_body) TODO: reenable when markdown is matchable in UI tests
        private val pinLetterImage = TestElement.Tag(testRule, R.drawable.ic_illustration_pin_letter.toString())

        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, pinLetterImage, back)
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
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
                return TestElement.Text(testRule, R.string.firstTimeUser_can_confirmTransportPIN_title, transportPin)
            }

//        private val body: TestElement TODO: reenable when markdown is matchable in UI tests
//            get() {
//                return TestElement.Text(testRule, R.string.firstTimeUser_can_confirmTransportPIN_body, transportPin)
//            }
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val inputCorrectBtn = TestElement.Text(testRule, R.string.firstTimeUser_can_confirmTransportPIN_confirmInput)
        val retryInputBtn = TestElement.Text(testRule, R.string.firstTimeUser_can_confirmTransportPIN_incorrectInput)
        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, cancel, inputCorrectBtn, retryInputBtn)
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    navigationConfirmDialog
                )
            }
    }

    data class SetupCanIntro(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = false
        fun setBackAllowed(value: Boolean) : SetupCanIntro {
            backAllowed = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupCanIntro {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.identification_can_intro_title)
//        private val body = TestElement.Text(testRule, R.string.identification_can_intro_body) TODO: reenable when markdown is matchable in UI tests
        val enterCanNowBtn = TestElement.Text(testRule, R.string.identification_can_intro_continue)
        private val canImage = TestElement.Tag(testRule, R.drawable.illustration_id_can.toString())

        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, canImage, enterCanNowBtn)
                    .plus(arrayOf(back).takeIf { backAllowed } ?: arrayOf())
                    .plus(arrayOf(cancel).takeIf { !backAllowed } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(navigationConfirmDialog)
                    .plus(arrayOf(back).takeIf { !backAllowed } ?: arrayOf())
                    .plus(arrayOf(cancel).takeIf { backAllowed } ?: arrayOf())
            }
    }

    data class SetupCanInput(override val testRule: ComposeTestRule) : TestScreen() {

        private var retry = false
        fun setRetry(value: Boolean) : SetupCanInput {
            retry = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.identification_can_input_title)
        private val body = TestElement.Text(testRule, R.string.identification_can_input_body)
        private val errorMessage = TestElement.Text(testRule, R.string.identification_can_incorrectInput_error_incorrect_body)
        private val retryMessage = TestElement.Text(testRule, R.string.identification_personalPIN_error_tryAgain)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val canEntryField = TestElement.Can(testRule)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, body, back, canEntryField)
                    .plus(arrayOf(retryMessage, errorMessage).takeIf { retry } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
                    .plus(arrayOf(retryMessage, errorMessage).takeIf { !retry } ?: arrayOf())
            }
    }

    data class SetupCanAlreadySetup(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupCanAlreadySetup {
            identPending = value
            return this
        }

        private val title = TestElement.Text(testRule, R.string.firstTimeUser_can_alreadySetup_title)

        // TODO: Use when markdwon is matchable
        private val bodyIdent = TestElement.Text(testRule, R.string.firstTimeUser_can_alreadySetup_body_ident)
        private val bodyNoIdent = TestElement.Text(testRule, R.string.firstTimeUser_can_alreadySetup_body_setup)

        val personalPinNotAvailableBtn = TestElement.Text(testRule, R.string.firstTimeUser_can_alreadySetup_personalPINNotAvailable)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)

        val finishSetupBtn = TestElement.Text(testRule, R.string.firstTimeUser_done_close)
        val identifyNowBtn = TestElement.Text(testRule, R.string.firstTimeUser_done_identify)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, back, personalPinNotAvailableBtn)
                    .plus(arrayOf(/*bodyIdent,*/ identifyNowBtn).takeIf { identPending } ?: arrayOf())
                    .plus(arrayOf(/*bodyNoIdent,*/ finishSetupBtn).takeIf { !identPending } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(TestElement.Tag(testRule, NavigationIcon.Cancel.name))
                    .plus(arrayOf(/*bodyIdent,*/ identifyNowBtn).takeIf { !identPending } ?: arrayOf())
                    .plus(arrayOf(/*bodyNoIdent,*/ finishSetupBtn).takeIf { identPending } ?: arrayOf())
            }
    }

    // ERROR SCREENS

    data class ErrorCardDeactivated(override val testRule: ComposeTestRule) : TestScreen() {

        val title = TestElement.Text(testRule, R.string.scanError_cardDeactivated_title)
        //        val body = TestElement.Text(R.string.scanError_cardDeactivated_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val closeBtn = TestElement.Text(testRule, R.string.scanError_close)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, cancel, closeBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name)
                )
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

        val title = TestElement.Text(testRule, R.string.scanError_cardUnreadable_title)
        //        val body = TestElement.Text(R.string.scanError_cardUnreadable_body) TODO: reenable when markdown is matchable in UI tests
        val errorCard = TestElement.BundCard(
            testRule,
            titleResId = R.string.scanError_box_title,
            bodyResId = R.string.scanError_box_body,
            iconTag = Icons.Filled.Dangerous.name
        )
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)

        val closeBtn: TestElement.Text
            get() {
                return TestElement.Text(testRule,
                    if (redirectUrlPresent)
                        R.string.scanError_redirect
                    else
                        R.string.scanError_close
                )
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, cancel, closeBtn
                ).plus(arrayOf(errorCard).takeIf { identPending } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(
                    TestElement.Tag(testRule, NavigationIcon.Back.name)
                ).plus(arrayOf(errorCard).takeIf { !identPending } ?: arrayOf())
            }
    }

    data class ErrorGenericError(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : ErrorGenericError {
            identPending = value
            return this
        }

        val title = TestElement.Text(testRule, R.string.scanError_unknown_title)
        //        val body = TestElement.Text(R.string.scanError_unknown_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val confirmationDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        val closeBtn: TestElement.Text
            get() {
                return TestElement.Text(testRule,
                    if (identPending)
                        R.string.identification_fetchMetadataError_retry
                    else
                        R.string.scanError_close
                )
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, cancel, closeBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name),
                    confirmationDialog
                )
            }
    }

    data class ErrorCardBlocked(override val testRule: ComposeTestRule) : TestScreen() {

        val title = TestElement.Text(testRule, R.string.scanError_cardBlocked_title)
        //        val body = TestElement.Text(R.string.scanError_cardBlocked_body) TODO: reenable when markdown is matchable in UI tests
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val closeBtn = TestElement.Text(testRule, R.string.scanError_close)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, cancel, closeBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name)
                )
            }
    }
}
