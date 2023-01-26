package de.digitalService.useID.util

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.digitalService.useID.MainActivity

typealias ComposeTestRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

const val PIN_ENTRY_FIELD_TAG = "PINEntryField"


fun ComposeTestRule.assertNodeWithResourceIdIsDisplayed(resId: Int) {
    val string = this.activity.getString(resId)
    this.onNodeWithText(string).assertIsDisplayed()
}

fun ComposeTestRule.clickOnNodeWithResourceId(resId: Int) {
    val string = this.activity.getString(resId)
    this.onNodeWithText(string).performClick()
}

fun ComposeTestRule.performPinInput(pin: String) {
    this.onNodeWithTag(PIN_ENTRY_FIELD_TAG).performTextInput(pin)
}

fun ComposeTestRule.assertPinFieldContains(input: String) {
    this.onNodeWithTag(PIN_ENTRY_FIELD_TAG).assertTextContains(input)
}

fun ComposeTestRule.pressReturn() {
    this.onNodeWithTag(PIN_ENTRY_FIELD_TAG).performImeAction()
}



