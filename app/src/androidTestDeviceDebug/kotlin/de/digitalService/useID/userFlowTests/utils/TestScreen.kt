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

        val titleImage = TestElement.Tag(testRule, R.drawable.abstract_widget_phone.toString())
        val headerTitle = TestElement.Text(testRule, R.string.home_header_title)
        val headerBody = TestElement.Text(testRule, R.string.home_header_body)

        val title = TestElement.Text(testRule, R.string.home_more_title)
        val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
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

        val title = TestElement.Text(testRule, R.string.firstTimeUser_intro_title)
//        val body = TestElement.Text(R.string.firstTimeUser_intro_body) TODO: reenable when markdown is matchable in UI tests
        val idsImage = TestElement.Tag(testRule, R.drawable.eid_3.toString())
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

        val title = TestElement.Text(testRule, R.string.firstTimeUser_pinLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_pinLetter_body) TODO: reenable when markdown is matchable in UI tests
        val pinLetterImage = TestElement.Tag(testRule, R.drawable.pin_letter.toString())
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

    data class TransportPin(override val testRule: ComposeTestRule) : TestScreen() {

        private var attemptsLeft = 3
        fun setAttemptsLeft(value: Int) : TransportPin {
            attemptsLeft = value
            return this
        }

        private var identPending = false
        fun setIdentPending(value: Boolean) : TransportPin {
            identPending = value
            return this
        }

        val title: TestElement.Text
            get() {
                return TestElement.Text(testRule, if (attemptsLeft == 2) {
                        R.string.firstTimeUser_incorrectTransportPIN_title
                    } else {
                        R.string.firstTimeUser_transportPIN_title
                    }
                )
            }

        val body = TestElement.Text(testRule, R.string.firstTimeUser_transportPIN_body)
        private val twoAttemptsLeftMessage = TestElement.Text(testRule, R.plurals.firstTimeUser_transportPIN_remainingAttempts, formatArg = "2", quantity = 2)
        private val oneAttemptLeftMessage = TestElement.Text(testRule, R.plurals.firstTimeUser_transportPIN_remainingAttempts, quantity = 1)

        val transportPinField = TestElement.TransportPin(testRule)
        val navigationIcon: TestElement.Tag
            get() {
                return TestElement.Tag(testRule, if (attemptsLeft == 2) {
                        NavigationIcon.Cancel.name
                    } else {
                        NavigationIcon.Back.name
                    }
                )
            }

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(title, body, transportPinField, navigationIcon)
                    .plus(arrayOf(oneAttemptLeftMessage).takeIf { attemptsLeft == 1 } ?: arrayOf())
                    .plus(arrayOf(twoAttemptsLeftMessage).takeIf { attemptsLeft == 2 } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(navigationConfirmDialog)
                    .plus(arrayOf(oneAttemptLeftMessage).takeIf { attemptsLeft != 1 } ?: arrayOf())
                    .plus(arrayOf(twoAttemptsLeftMessage).takeIf { attemptsLeft != 2 } ?: arrayOf())
            }
    }

    data class SetupPersonalPinIntro(override val testRule: ComposeTestRule) : TestScreen() {

        val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_title)
        val card = TestElement.BundCard(
            testRule,
            titleResId = R.string.firstTimeUser_personalPINIntro_info_title,
            bodyResId = R.string.firstTimeUser_personalPINIntro_info_body,
            iconTag = Icons.Filled.Info.name
        )
        val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val continueBtn = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_continue)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, card, idsImage, back, continueBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name)
                )
            }
    }

    data class SetupPersonalPinInput(override val testRule: ComposeTestRule) : TestScreen() {
        // Components of this screen
        val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_title)
        val body = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val personalPinField = TestElement.PersonalPin(testRule)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, body, personalPinField, back,
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name)
                )
            }
    }

    data class SetupPersonalPinConfirm(override val testRule: ComposeTestRule) : TestScreen() {

        private var error = false
        fun setError(value: Boolean) : SetupPersonalPinConfirm {
            error = value
            return this
        }

        val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_title)
        val body = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val errorMsg = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_error_mismatch_title)
        val tryAgainBtn = TestElement.Text(testRule, R.string.identification_fetchMetadataError_retry)
        val personalPinField = TestElement.PersonalPin(testRule)
        val pinsDontMatchDialog = TestElement.StandardDialog(
            testRule,
            titleResId = R.string.firstTimeUser_personalPIN_error_mismatch_title,
            dismissBtnId = R.string.identification_fetchMetadataError_retry
        )

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, body, personalPinField, back
                ).plus(arrayOf(errorMsg, tryAgainBtn).takeIf { error } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get()  {
                return arrayOf<TestElement>(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name),
                    pinsDontMatchDialog
                ).plus(arrayOf(errorMsg, tryAgainBtn).takeIf { !error } ?: arrayOf())
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

        val title = TestElement.Text(testRule, R.string.firstTimeUser_scan_title)
        val body = TestElement.Text(testRule, R.string.firstTimeUser_scan_body)
        val navigationIcon: TestElement.Tag
            get() {
                return TestElement.Tag(testRule, if (backAllowed) {
                        NavigationIcon.Back.name
                    } else {
                        NavigationIcon.Cancel.name
                    }
                )
            }
        val progressIndicator = TestElement.Tag(testRule, progressIndicatorTag)
        val nfcHelpBtn = TestElement.Text(testRule, R.string.scan_helpNFC)
        val scanHelpBtn = TestElement.Text(testRule, R.string.scan_helpScanning)
        val navigationConfirmaDialog:  TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

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
                return arrayOf(
                    title, body, navigationIcon, nfcHelpBtn, scanHelpBtn
                ).plus(arrayOf(progressIndicator).takeIf { progress } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf<TestElement>(
                    navigationConfirmaDialog, nfcDialog, helpDialog
                ).plus(arrayOf<TestElement>(progressIndicator).takeIf { !progress } ?: arrayOf())
            }
    }

    data class SetupFinish(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun setIdentPending(value: Boolean) : SetupFinish {
            identPending = value
            return this
        }

        val title = TestElement.Text(testRule, R.string.firstTimeUser_done_title)
        val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val cancel = TestElement.Tag(testRule, NavigationIcon.Cancel.name)
        val finishSetupBtn:  TestElement.Text
            get() {
                return TestElement.Text(testRule, if (identPending) R.string.firstTimeUser_done_identify else R.string.firstTimeUser_done_close)
            }

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, idsImage, cancel, finishSetupBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Back.name), navigationConfirmDialog
                )
            }
    }

    data class ResetPersonalPin(override val testRule: ComposeTestRule) : TestScreen() {

        val title = TestElement.Text(testRule, R.string.firstTimeUser_missingPINLetter_title)
        //        val body = TestElement.Text(R.string.firstTimeUser_missingPINLetter_body) TODO: reenable when markdown is matchable in UI tests
        val pinLetterImage = TestElement.Tag(testRule, R.drawable.ic_illustration_pin_letter.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, pinLetterImage, back
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name)
                )
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

        val title: TestElement
            get() {
                return TestElement.Text(testRule, R.string.firstTimeUser_can_confirmTransportPIN_title, transportPin)
            }

//        val body: TestElement TODO: reenable when markdown is matchable in UI tests
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
                return arrayOf(
                    title, cancel, inputCorrectBtn, retryInputBtn
                )
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

        val title = TestElement.Text(testRule, R.string.identification_can_intro_title)
//        val body = TestElement.Text(testRule, R.string.identification_can_intro_body) TODO: reenable when markdown is matchable in UI tests
        val enterCanNowBtn = TestElement.Text(testRule, R.string.identification_can_intro_continue)
        val canImage = TestElement.Tag(testRule, R.drawable.illustration_id_can.toString())

        val navigationIcon: TestElement.Tag
            get() {
                return TestElement.Tag(testRule, if (backAllowed) {
                        NavigationIcon.Back.name
                    } else {
                        NavigationIcon.Cancel.name
                    }
                )
            }

        val navigationConfirmDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(testRule, identPending)
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, navigationIcon, canImage, enterCanNowBtn
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    navigationConfirmDialog
                )
            }
    }

    data class SetupCanInput(override val testRule: ComposeTestRule) : TestScreen() {

        private var retry = false
        fun setRetry(value: Boolean) : SetupCanInput {
            retry = value
            return this
        }

        val title = TestElement.Text(testRule, R.string.identification_can_input_title)
        val body = TestElement.Text(testRule, R.string.identification_can_input_body)
        val errorMessage = TestElement.Text(testRule, R.string.identification_can_incorrectInput_error_incorrect_body)
        val retryMessage = TestElement.Text(testRule, R.string.identification_personalPIN_error_tryAgain)
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
