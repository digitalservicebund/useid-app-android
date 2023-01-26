package de.digitalService.useID.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed

fun SemanticsNodeInteraction.assertIsDisplayedDetailed(nodeName: String) {
    try {
        assertIsDisplayed()
    } catch (e: AssertionError) {
        throw AssertionError("Assert failed: [$nodeName] is not displayed!")
    }
}

fun SemanticsNodeInteraction.safeAssertIsNotDisplayed() {
    try {
        assertDoesNotExist()
    } catch (e: AssertionError) {
        assertIsNotDisplayed()
    }
}
