package de.digitalService.useID.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.performScrollTo


fun SemanticsNodeInteraction.assertIsDisplayedWithScrolling(nodeName: String) {
    try {
        assertIsDisplayed()
    } catch (e: AssertionError) {
        try {
            performScrollTo()
            assertIsDisplayed()
        } catch (e: AssertionError) {
            throw AssertionError("Assert failed: [$nodeName] is not displayed even after scrolling! \n${e.message}")
        }
    }
}

fun SemanticsNodeInteraction.safeAssertIsNotDisplayed(nodeName: String) {
    try {
        assertDoesNotExist()
    } catch (e: AssertionError) {
        try {
            assertIsNotDisplayed()
        } catch (e: AssertionError) {
            throw AssertionError("Assert failed: [$nodeName] is displayed!")
        }
    }
}
