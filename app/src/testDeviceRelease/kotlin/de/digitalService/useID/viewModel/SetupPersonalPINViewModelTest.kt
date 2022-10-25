package de.digitalService.useID.viewModel

import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupPersonalPINViewModel
import de.digitalService.useID.ui.screens.setup.SetupPersonalPINViewModelInterface
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class SetupPersonalPINViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var coordinatorMock: SetupCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    val defaultValue = ""

    @BeforeEach
    fun setUp() {
        mockkStatic(android.text.TextUtils::class)
        val slot = slot<String>()
        every { android.text.TextUtils.isDigitsOnly(capture(slot)) } answers { Regex("[0-9]*").matches(slot.captured) }
    }

    @Nested
    inner class UserInputPIN1 {
        @Test
        fun success() {
            val testValue = "123456"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue)

            assertEquals(testValue, viewModel.pin1)
            assertTrue(viewModel.shouldShowPIN2EntryField)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_2, viewModel.focus)
        }

        @Test
        fun tooShort() {
            val testValue = "12345"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue)

            assertEquals(testValue, viewModel.pin1)
            assertFalse(viewModel.shouldShowPIN2EntryField)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)
        }

        @Test
        fun empty() {
            val testValue = ""

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue)

            assertEquals(testValue, viewModel.pin1)
            assertFalse(viewModel.shouldShowPIN2EntryField)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)
        }

        @ParameterizedTest
        @ValueSource(strings = ["abcdef", "12345A", "123456.", "--12--", "      ", "\n\n\n\n\n\n", "12345\n", "123 56", " 123456 "])
        fun notNumericOnly(testValue: String) {
            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue)

            assertEquals("", viewModel.pin1)
            assertFalse(viewModel.shouldShowPIN2EntryField)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)
        }

        @Test
        fun emoji() {
            val emoji = String(intArrayOf(0x274C), 0, 1)
            val testValue = "$emoji$emoji$emoji$emoji$emoji$emoji"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue)

            assertEquals("", viewModel.pin1)
            assertFalse(viewModel.shouldShowPIN2EntryField)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)
        }
    }

    @Nested
    inner class UserInputPIN2 {
        @Test
        fun success() {
            val testValue = "123456"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)

            viewModel.userInputPIN1(testValue)
            viewModel.userInputPIN2(testValue)

            assertEquals("", viewModel.pin1)
            assertEquals("", viewModel.pin2)

            verify(exactly = 1) { coordinatorMock.onPersonalPINEntered(testValue) }
        }

        @Test
        fun tooShort() {
            val testValue = "12345"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)

            viewModel.userInputPIN1(testValue)
            viewModel.userInputPIN2(testValue)

            assertEquals(testValue, viewModel.pin1)
            assertEquals(testValue, viewModel.pin2)

            verify(exactly = 0) { coordinatorMock.onPersonalPINEntered(any()) }
        }

        @Test
        fun empty() {
            val testValue = ""

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)

            viewModel.userInputPIN1(testValue)
            viewModel.userInputPIN2(testValue)

            assertEquals(testValue, viewModel.pin1)
            assertEquals(testValue, viewModel.pin2)

            verify(exactly = 0) { coordinatorMock.onPersonalPINEntered(any()) }
        }

        @ParameterizedTest
        @ValueSource(strings = ["abcdef", "12345A", "123456.", "--12--", "      ", "\n\n\n\n\n\n", "12345\n", "123 56", " 123456 "])
        fun notNumericOnly(testValue: String) {
            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)

            viewModel.userInputPIN1(testValue)
            viewModel.userInputPIN2(testValue)

            assertEquals("", viewModel.pin1)
            assertEquals("", viewModel.pin2)

            verify(exactly = 0) { coordinatorMock.onPersonalPINEntered(any()) }
        }

        @Test
        fun emoji() {
            val emoji = String(intArrayOf(0x274C), 0, 1)
            val testValue = "$emoji$emoji$emoji$emoji$emoji$emoji"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)

            viewModel.userInputPIN1(testValue)
            viewModel.userInputPIN2(testValue)

            assertEquals("", viewModel.pin1)
            assertEquals("", viewModel.pin2)

            verify(exactly = 0) { coordinatorMock.onPersonalPINEntered(any()) }
        }

        @Test
        fun notEqual() {
            val testValue1 = "123456"
            val testValue2 = "123450"

            val viewModel = SetupPersonalPINViewModel(
                coordinatorMock,
                mockTrackerManager
            )

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)

            viewModel.userInputPIN1(testValue1)

            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_2, viewModel.focus)

            viewModel.userInputPIN2(testValue2)

            assertEquals(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1, viewModel.focus)
            assertTrue(viewModel.shouldShowError)

            assertEquals(defaultValue, viewModel.pin1)
            assertEquals(defaultValue, viewModel.pin2)

            verify(exactly = 0) { coordinatorMock.onPersonalPINEntered(any()) }
        }
    }
}
