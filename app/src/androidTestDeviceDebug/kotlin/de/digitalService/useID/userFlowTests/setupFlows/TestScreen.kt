package de.digitalService.useID.userFlowTests.setupFlows

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.*
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.util.*
import org.junit.Assert

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
                    //            Not displayed as they are outside of the screen area
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

    data class SetupTransportPin(override val testRule: ComposeTestRule) : TestScreen() {

        private var retry = false
        fun retry(value: Boolean) : SetupTransportPin {
            retry = value
            return this
        }

        val title: TestElement.Text
            get() {
                return TestElement.Text(testRule, if (retry) {
                        R.string.firstTimeUser_incorrectTransportPIN_title
                    } else {
                        R.string.firstTimeUser_transportPIN_title
                    }
                )
            }

        val body = TestElement.Text(testRule, R.string.firstTimeUser_transportPIN_body)
        val transportPinField = TestElement.TransportPin(testRule)
//        val pin_letter_image = TestElement.Tag(R.drawable.pin_letter.toString())
        val navigationIcon: TestElement.Tag
            get() {
                return TestElement.Tag(testRule, if (retry) {
                        NavigationIcon.Cancel.name
                    } else {
                        NavigationIcon.Back.name
                    }
                )
            }

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, body, transportPinField, navigationIcon
                )
            }

        override val unexpectedElements: Array<TestElement>
            get() {
                return arrayOf()
            }
    }

    data class SetupPersonalPinIntro(override val testRule: ComposeTestRule) : TestScreen() {

        val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_title)
        val cardTitle = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_info_title)
        val cardBody = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_info_body)
        val cardIcon = TestElement.Tag(testRule, Icons.Filled.Info.name)
        val idsImage = TestElement.Tag(testRule, R.drawable.eid_3_pin.toString())
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val continueBtn = TestElement.Text(testRule, R.string.firstTimeUser_personalPINIntro_continue)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, cardTitle, cardBody, cardIcon, idsImage, back, continueBtn
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
        fun error(value: Boolean) : SetupPersonalPinConfirm {
            error = value
            return this
        }

        val title = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_title)
        val body = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_confirmation_body)
        val back = TestElement.Tag(testRule, NavigationIcon.Back.name)
        val errorMsg = TestElement.Text(testRule, R.string.firstTimeUser_personalPIN_error_mismatch_title)
        val tryAgainBtn = TestElement.Text(testRule, R.string.identification_fetchMetadataError_retry)
        val personalPinField = TestElement.PersonalPin(testRule)

        override val expectedElements: Array<TestElement>
            get() {
                return arrayOf(
                    title, body, personalPinField, back
                ).plus(arrayOf(errorMsg, tryAgainBtn).takeIf { error } ?: arrayOf())
            }

        override val unexpectedElements: Array<TestElement>
            get()  {
                return arrayOf<TestElement>(
                    TestElement.Tag(testRule, NavigationIcon.Cancel.name)
                ).plus(arrayOf(errorMsg, tryAgainBtn).takeIf { !error } ?: arrayOf())
            }
    }

    data class SetupScan(override val testRule: ComposeTestRule) : TestScreen() {

        private var backAllowed = true
        fun backAllowed(value: Boolean) : SetupScan {
            backAllowed = value
            return this
        }

        private var identPending = false
        fun identPending(value: Boolean) : SetupScan {
            identPending = value
            return this
        }

        private var progress = false
        fun progress(value: Boolean) : SetupScan {
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
        val confirmationDialog:  TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(
                    testRule,
                    titleResId = if (identPending) R.string.identification_confirmEnd_title else R.string.firstTimeUser_confirmEnd_title,
                    messageResId = if (identPending) R.string.firstTimeUser_confirmEnd_message else R.string.identification_confirmEnd_message,
                    confirmBtnId = if (identPending) R.string.identification_confirmEnd_confirm else R.string.firstTimeUser_confirmEnd_confirm,
                    dismissBtnId = if (identPending) R.string.identification_confirmEnd_deny else R.string.firstTimeUser_confirmEnd_deny
                )
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
                    confirmationDialog, nfcDialog, helpDialog
                ).plus(arrayOf<TestElement>(progressIndicator).takeIf { !progress } ?: arrayOf())
            }
    }

    data class SetupFinish(override val testRule: ComposeTestRule) : TestScreen() {

        private var identPending = false
        fun identPending(value: Boolean) : SetupFinish {
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

        val confirmationDialog: TestElement.NavigationConfirmDialog
            get() {
                return TestElement.NavigationConfirmDialog(
                    testRule,
                    titleResId = if (identPending) R.string.identification_confirmEnd_title else R.string.firstTimeUser_confirmEnd_title,
                    messageResId = if (identPending) R.string.firstTimeUser_confirmEnd_message else R.string.identification_confirmEnd_message,
                    confirmBtnId = if (identPending) R.string.identification_confirmEnd_confirm else R.string.firstTimeUser_confirmEnd_confirm,
                    dismissBtnId = if (identPending) R.string.identification_confirmEnd_deny else R.string.firstTimeUser_confirmEnd_deny
                )
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
                    TestElement.Tag(testRule, NavigationIcon.Back.name), confirmationDialog
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
}


sealed class TestElement {

    val pinEntryFieldTestTag = "PINEntryField" // Hidden textfield used for entry
    val underscoreTestTag = "PINDigitField" // Underscore
    val obfuscationTestTag = "Obfuscation" // Obfuscation dot
    val digitTestTag = "PINEntry" // Cleartext digit
    val spacerTestTag = "PINDigitRowSpacer"

    abstract val testRule: ComposeTestRule
    abstract fun assertIsDisplayed()
    abstract fun assertIsNotDisplayed()
    open fun click() {
        Assert.fail("click() not implemented for $this")
    }

    data class Text(override val testRule: ComposeTestRule, val resourceId: Int) : TestElement() {
        override fun assertIsDisplayed() {
            val string = testRule.activity.getString(resourceId)
            testRule.onNodeWithText(string).assertIsDisplayedDetailed(string)
        }

        override fun assertIsNotDisplayed() {
            val string = testRule.activity.getString(resourceId)
            testRule.onNodeWithText(string).safeAssertIsNotDisplayed()
        }

        override fun click() {
            val string = testRule.activity.getString(resourceId)
            testRule.onNodeWithText(string).performClick()
        }
    }

    data class Tag(override val testRule: ComposeTestRule, val tag: String) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithTag(tag).assertIsDisplayedDetailed(tag)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithTag(tag).safeAssertIsNotDisplayed()
        }

        override fun click() {
            testRule.onNodeWithTag(tag).performClick()
        }
    }

    data class NavigationConfirmDialog(
        override val testRule: ComposeTestRule,
        val titleResId: Int,
        val messageResId: Int,
        val confirmBtnId: Int,
        val dismissBtnId: Int
    ) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).assertIsDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(messageResId)).assertIsDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(confirmBtnId)).assertIsDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).assertIsDisplayed()
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(messageResId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(confirmBtnId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).safeAssertIsNotDisplayed()
        }

        fun confirm() {
            testRule.onNodeWithText(testRule.activity.getString(confirmBtnId)).performClick()
        }

        fun dismiss() {
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).performClick()
        }
    }

    data class StandardDialog(
        override val testRule: ComposeTestRule,
        val titleResId: Int,
        val dismissBtnId: Int
    ) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).assertIsDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).assertIsDisplayed()
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).safeAssertIsNotDisplayed()
        }

        fun dismiss() {
            testRule.onNodeWithText(testRule.activity.getString(dismissBtnId)).performClick()
        }
    }

    data class TransportPin(
        override val testRule: ComposeTestRule
    ) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithTag(pinEntryFieldTestTag).assertIsDisplayed()
            testRule.onAllNodesWithTag(underscoreTestTag).assertCountEquals(5)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithTag(pinEntryFieldTestTag).assertIsDisplayed()
            testRule.onAllNodesWithTag(underscoreTestTag).assertCountEquals(0)
        }

        fun assertLength(len: Int) {
            assertIsDisplayed()
            testRule.onAllNodesWithTag(digitTestTag).assertCountEquals(len)
            testRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)
        }
    }

    data class PersonalPin(
        override val testRule: ComposeTestRule
    ) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithTag(pinEntryFieldTestTag).assertIsDisplayed()
            testRule.onAllNodesWithTag(underscoreTestTag).assertCountEquals(6)
            testRule.onAllNodesWithTag(spacerTestTag).assertCountEquals(1)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithTag(pinEntryFieldTestTag).assertIsDisplayed()
            testRule.onAllNodesWithTag(underscoreTestTag).assertCountEquals(0)
        }

        fun assertLength(len: Int) {
            assertIsDisplayed()
            testRule.onAllNodesWithTag(digitTestTag).assertCountEquals(0)
            testRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(len)
        }
    }
}
