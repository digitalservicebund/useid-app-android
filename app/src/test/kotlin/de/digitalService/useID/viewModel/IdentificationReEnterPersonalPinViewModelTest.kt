package de.digitalService.useID.viewModel

import androidx.core.text.isDigitsOnly
import de.digitalService.useID.ui.composables.screens.identification.IdentificationReEnterPersonalPINViewModel
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationReEnterPersonalPinViewModelTest {

    @Test
    fun userInputPIN() {
        val testValue = "12345"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk()

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationReEnterPersonalPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.userInputPIN(testValue)

        assertEquals(testValue, viewModel.pin)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }

    @Test
    fun onDoneTapped_Success() {
        val testValue = "123456"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationReEnterPersonalPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDoneTapped()

        assertEquals(testValue, viewModel.pin)

        verify(exactly = 1) { mockCallback.invoke(testValue) }
    }

    @Test
    fun onDoneTapped_tooShort() {
        val testValue = "1234"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationReEnterPersonalPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDoneTapped()

        assertEquals(testValue, viewModel.pin)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }

    @Test
    fun onDoneTapped_tooLong() {
        val testValue = "1234567"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationReEnterPersonalPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDoneTapped()

        assertEquals("", viewModel.pin)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }
}
