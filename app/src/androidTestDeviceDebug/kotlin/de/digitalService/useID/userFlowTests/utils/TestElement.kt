package de.digitalService.useID.userFlowTests.utils

import androidx.compose.ui.test.*
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.assertIsDisplayedDetailed
import de.digitalService.useID.util.safeAssertIsNotDisplayed
import de.digitalService.useID.R
import de.digitalService.useID.util.assertIsDisplayedWithScrolling
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

    open fun scrollToAndClick() {
        Assert.fail("scrollToAndClick() not implemented for $this")
    }

    data class Text(
        override val testRule: ComposeTestRule,
        val text: String? = null,
        val resourceId: Int? = null,
        val formatArg: String? = null,
        val quantity: Int? = null
    ) : TestElement() {

        private fun getStringToTestAgainst(): String {
            return if (resourceId == null && text != null) {
                text
            } else if (resourceId != null && text == null) {
                if (quantity != null) {
                    testRule.activity.resources.getQuantityString(resourceId, quantity, formatArg?.toInt())
                } else {
                    testRule.activity.getString(resourceId, formatArg)
                }
            } else {
                Assert.fail("String/ResourceId to test against either not provided or abigious")
                ""
            }
        }

        val string = getStringToTestAgainst()

        override fun assertIsDisplayed() {
            testRule.onNodeWithText(string).assertIsDisplayedWithScrolling(string)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithText(string).safeAssertIsNotDisplayed()
        }

        override fun click() {
            testRule.onNodeWithText(string).performClick()
        }

        override fun scrollToAndClick() {
            testRule.onNodeWithText(string).performScrollTo().performClick()
        }
    }

    data class Tag(override val testRule: ComposeTestRule, val tag: String) : TestElement() {

        override fun assertIsDisplayed() {
            testRule.onNodeWithTag(tag).assertIsDisplayedWithScrolling(tag)
        }

        override fun assertIsNotDisplayed() {
            testRule.onNodeWithTag(tag).safeAssertIsNotDisplayed()
        }

        override fun click() {
            testRule.onNodeWithTag(tag).performClick()
        }

        override fun scrollToAndClick() {
            testRule.onNodeWithTag(tag).performScrollTo().performClick()
        }
    }

    data class Group(override val testRule: ComposeTestRule, val elements: List<TestElement>) : TestElement() {

        override fun assertIsDisplayed() {
            elements.forEach {
                it.assertIsDisplayed()
            }
        }

        override fun assertIsNotDisplayed() {
            elements.forEach {
                it.assertIsNotDisplayed()
            }
        }
    }


    data class BundCard(
        override val testRule: ComposeTestRule,
        val titleResId: Int,
        val bodyResId: Int,
        val iconTag: String
    ) : TestElement() {
        override fun assertIsDisplayed() {
            val title = testRule.activity.getString(titleResId)
            testRule.onNodeWithText(title).assertIsDisplayedWithScrolling(title)
            val body = testRule.activity.getString(bodyResId)
            testRule.onNodeWithText(body).assertIsDisplayedWithScrolling(body)
            testRule.onNodeWithTag(iconTag).assertIsDisplayedWithScrolling(iconTag)
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

    data class Can(
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
            testRule.onAllNodesWithTag(digitTestTag).assertCountEquals(len)
            testRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)
        }
    }
}
