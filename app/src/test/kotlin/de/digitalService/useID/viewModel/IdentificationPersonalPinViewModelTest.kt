package de.digitalService.useID.viewModel

import androidx.core.text.isDigitsOnly
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPINViewModel
import de.digitalService.useID.ui.composables.screens.identification.IdentificationPersonalPINViewModel
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationPersonalPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @Test
    fun userInputPIN_1() {
        val testValue = "12345"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        assertEquals(testValue ,viewModel.pin)
    }

    @Test
    fun userInputPIN_2() {
        val testValue = "1"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        assertEquals(testValue ,viewModel.pin)
    }

    @Test
    fun userInputPIN_3() {
        val testValue = "1234567"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        assertEquals("" ,viewModel.pin)
    }

    @Test
    fun userInputPIN_4() {
        val testValue = "123A5"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns false

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        assertEquals("" ,viewModel.pin)
    }

    @Test
    fun onDone_1() {
        val testValue = "123456"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDone()

        assertEquals(testValue ,viewModel.pin)

        verify(exactly = 1) { mockIdentificationCoordinator.onPINEntered(testValue) }
    }

    @Test
    fun onDone_2() {
        val testValue = "12345"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDone()

        assertEquals(testValue ,viewModel.pin)

        verify(exactly = 0) { mockIdentificationCoordinator.onPINEntered(testValue) }
    }

    @Test
    fun onDone_3() {
        val testValue = "1234567"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPINViewModel(
            mockIdentificationCoordinator
        )

        viewModel.userInputPIN(testValue)
        viewModel.onDone()

        assertEquals("" ,viewModel.pin)

        verify(exactly = 0) { mockIdentificationCoordinator.onPINEntered(testValue) }
    }
}
