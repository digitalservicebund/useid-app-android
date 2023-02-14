package de.digitalService.useID.userFlowTests.utils

import androidx.compose.ui.test.*
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.assertIsDisplayedDetailed
import de.digitalService.useID.util.safeAssertIsNotDisplayed
import de.digitalService.useID.R
import org.junit.Assert

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

    data class BundCard(
        override val testRule: ComposeTestRule,
        val titleResId: Int,
        val bodyResId: Int,
        val iconTag: String
    ) : TestElement() {
        override fun assertIsDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).assertIsDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(bodyResId)).assertIsDisplayed()
            testRule.onNodeWithTag(iconTag).assertIsDisplayedDetailed(iconTag)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithText(testRule.activity.getString(titleResId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithText(testRule.activity.getString(bodyResId)).safeAssertIsNotDisplayed()
            testRule.onNodeWithTag(iconTag).safeAssertIsNotDisplayed()
        }
    }

    data class NavigationConfirmDialog(
        override val testRule: ComposeTestRule,
        val identPending: Boolean
    ) : TestElement() {

        private val titleResId = if (identPending) R.string.identification_confirmEnd_title else R.string.firstTimeUser_confirmEnd_title
        private val messageResId = if (identPending) R.string.identification_confirmEnd_message else R.string.firstTimeUser_confirmEnd_message
        private val confirmBtnId = if (identPending) R.string.identification_confirmEnd_confirm else R.string.firstTimeUser_confirmEnd_confirm
        private val dismissBtnId = if (identPending) R.string.identification_confirmEnd_deny else R.string.firstTimeUser_confirmEnd_deny

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
