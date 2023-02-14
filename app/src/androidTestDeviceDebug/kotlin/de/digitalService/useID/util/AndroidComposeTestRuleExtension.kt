package de.digitalService.useID.util

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.digitalService.useID.MainActivity

typealias ComposeTestRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

const val PIN_ENTRY_FIELD_TAG = "PINEntryField"

fun ComposeTestRule.performPinInput(pin: String) {
    this.onNodeWithTag(PIN_ENTRY_FIELD_TAG).performTextInput(pin)
}

fun ComposeTestRule.pressReturn() {
    this.onNodeWithTag(PIN_ENTRY_FIELD_TAG).performImeAction()
}



