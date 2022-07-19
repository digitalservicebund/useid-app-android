package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPINViewModel
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupReEnterTransportPINViewModelTest {

    @Test
    fun onInputChanged() {
        val testValue = "12345"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk()

        val viewModel = SetupReEnterTransportPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.onInputChanged(testValue)

        assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }

    @Test
    fun onDoneTapped_Success() {
        val testValue = "12345"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        val viewModel = SetupReEnterTransportPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 1) { mockCallback.invoke(any()) }
    }

    @Test
    fun onDoneTapped_tooShort() {
        val testValue = "1234"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        val viewModel = SetupReEnterTransportPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }

    @Test
    fun onDoneTapped_tooLong() {
        val testValue = "123456"

        val testAttempts = 0
        val mockCallback: (String) -> Unit = mockk(relaxed = true)

        val viewModel = SetupReEnterTransportPINViewModel(
            testAttempts,
            mockCallback
        )

        viewModel.onInputChanged(testValue)
        viewModel.onDoneTapped()

        assertEquals(testValue, viewModel.transportPIN)

        verify(exactly = 0) { mockCallback.invoke(any()) }
    }
}
