package de.digitalService.useID

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ScreenWithTopBarTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun backNavigation() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Back,
                    onClick = mockCallback,
                    confirmation = null
                )
            ) {}
        }
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithText(cancelButton).assertDoesNotExist()

        verify(exactly = 1) { mockCallback.invoke() }
    }

    @Test
    fun cancelNavigation() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = null
                )
            ) {}
        }
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithText(cancelButton).assertDoesNotExist()

        verify(exactly = 1) { mockCallback.invoke() }
    }

    @Test
    fun cancelNavigationWithConfirmDialog() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = Flow.Identification
                )
            ) {}
        }
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()

        verify(exactly = 0) { mockCallback.invoke() }

        val confirmButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton).performClick()

        verify(exactly = 1) { mockCallback.invoke() }

        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()
        val dismissButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_deny)
        composeTestRule.onNodeWithText(dismissButton).performClick()

        verify(exactly = 1) { mockCallback.invoke() }

        composeTestRule.onNodeWithText(dismissButton).assertDoesNotExist()
        composeTestRule.onNodeWithText(confirmButton).assertDoesNotExist()
    }

    @Test
    fun cancelNavigationWithConfirmDialogPressBackInDialog() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = Flow.Identification
                )
            ) {}
        }
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()

        verify(exactly = 0) { mockCallback.invoke() }

        Espresso.pressBack()

        verify(exactly = 0) { mockCallback.invoke() }

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithText(cancelButton).assertDoesNotExist()

        verify(exactly = 0) { mockCallback.invoke() }
    }

    @Test
    fun cancelNavigationWithConfirmDialogPressBackToOpenDialog() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = Flow.Identification,
                )
            ) {}
        }
        Espresso.pressBack()

        verify(exactly = 0) { mockCallback.invoke() }

        val cancelButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(cancelButton).performClick()

        verify(exactly = 1) { mockCallback.invoke() }
    }

    @Test
    fun identificationDialog() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = Flow.Identification
                )
            ) {}
        }
        Espresso.pressBack()

        val confirmButton = composeTestRule.activity.getString(R.string.identification_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton).assertIsDisplayed()

        val dismissButton = composeTestRule.activity.getString(R.string.identification_confirmEnd_deny)
        composeTestRule.onNodeWithText(dismissButton).assertIsDisplayed()

        val title = composeTestRule.activity.getString(R.string.identification_confirmEnd_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()

        val body = composeTestRule.activity.getString(R.string.identification_confirmEnd_message)
        composeTestRule.onNodeWithText(body).assertIsDisplayed()
    }

    @Test
    fun setupDialog() {
        val mockCallback: () -> Unit = mockk()
        every { mockCallback.invoke() } returns Unit

        composeTestRule.activity.setContentUsingUseIdTheme {
            ScreenWithTopBar(
                navigationButton = NavigationButton(
                    icon = NavigationIcon.Cancel,
                    onClick = mockCallback,
                    confirmation = Flow.Setup,
                )
            ) {}
        }
        Espresso.pressBack()

        val confirmButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton).assertIsDisplayed()

        val dismissButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_deny)
        composeTestRule.onNodeWithText(dismissButton).assertIsDisplayed()

        val title = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()

        val body = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_message)
        composeTestRule.onNodeWithText(body).assertIsDisplayed()
    }
}
