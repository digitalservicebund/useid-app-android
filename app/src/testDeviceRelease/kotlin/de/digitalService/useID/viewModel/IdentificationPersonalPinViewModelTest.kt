package de.digitalService.useID.viewModel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinNavArgs
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class IdentificationPersonalPinViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    private val mockNavArgs: IdentificationPersonalPinNavArgs = mockk()

    @BeforeEach
    fun beforeEach() {
        mockkObject(IdentificationPersonalPinDestination)
        every { IdentificationPersonalPinDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs

        every { mockNavArgs.attempts } returns null
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 5, 8, 13, -123])
    fun readNavArgsCorrect_Ints(testValue: Int) {
        every { mockNavArgs.attempts } returns testValue

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        assertEquals(testValue, viewModel.attempts)
    }

    @Test
    fun readNavArgsCorrect_Null() {
        every { mockNavArgs.attempts } returns null

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        assertNull(viewModel.attempts)
    }

    @Test
    fun userInputPin_DisplayCorrect_5Digits() {
        val testValue = "12345"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        assertEquals(testValue, viewModel.pin)
    }

    @Test
    fun userInputPin_DisplayCorrect_SingleDigits() {
        val testValue = "1"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        assertEquals(testValue, viewModel.pin)
    }

    @Test
    fun userInputPin_DisplayNothing_TooLong() {
        val testValue = "1234567"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        assertEquals("", viewModel.pin)
    }

    @Test
    fun userInputPin_DisplayNothing_NotAllDigits() {
        val testValue = "123A5"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns false

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        assertEquals("", viewModel.pin)
    }

    @Test
    fun onDone_Success_ValidPin() {
        val testValue = "123456"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        viewModel.onDone()

        assertEquals(testValue, viewModel.pin)

        verify(exactly = 1) { mockIdentificationCoordinator.onPinEntered(testValue) }
    }

    @Test
    fun onDone_Failed_TooShort() {
        val testValue = "12345"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        viewModel.onDone()

        assertEquals(testValue, viewModel.pin)

        verify(exactly = 0) { mockIdentificationCoordinator.onPinEntered(testValue) }
    }

    @Test
    fun onDone_Failed_TooLong() {
        val testValue = "1234567"

        mockkStatic("android.text.TextUtils")
        every { testValue.isDigitsOnly() } returns true

        val viewModel = IdentificationPersonalPinViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.userInputPin(testValue)
        viewModel.onDone()

        assertEquals("", viewModel.pin)

        verify(exactly = 0) { mockIdentificationCoordinator.onPinEntered(testValue) }
    }
}
