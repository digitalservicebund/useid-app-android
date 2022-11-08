package de.digitalService.useID.viewModel

import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInputViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class SetupPersonalPinInputViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockCoordinator: SetupCoordinator

    private val defaultValue = ""

    @BeforeEach
    fun setUp() {
        mockkStatic(android.text.TextUtils::class)
        val slot = slot<String>()
        every { android.text.TextUtils.isDigitsOnly(capture(slot)) } answers { Regex("[0-9]*").matches(slot.captured) }
    }

    @Test
    fun success() {
        val testValue = "123456"

        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        assertEquals(defaultValue, viewModel.pin)

        viewModel.userInputPIN(testValue)

        assertEquals(testValue, viewModel.pin)
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }

        viewModel.onDonePressed()

        assertEquals(testValue, viewModel.pin)
        verify(exactly = 1) { mockCoordinator.onPersonalPinInput(testValue) }
    }

    @Test
    fun tooShort() {
        val testValue = "12345"

        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        assertEquals(defaultValue, viewModel.pin)

        viewModel.userInputPIN(testValue)

        assertEquals(testValue, viewModel.pin)
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }
        verify(exactly = 0) { mockCoordinator.confirmPersonalPin(testValue) }

        viewModel.onDonePressed()
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }
        verify(exactly = 0) { mockCoordinator.confirmPersonalPin(testValue) }
    }

    @Test
    fun empty() {
        val testValue = ""

        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        assertEquals(defaultValue, viewModel.pin)

        viewModel.userInputPIN(testValue)

        assertEquals(testValue, viewModel.pin)
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["abcdef", "12345A", "123456.", "--12--", "      ", "\n\n\n\n\n\n", "12345\n", "123 56", " 123456 "])
    fun notNumericOnly(testValue: String) {
        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        assertEquals(defaultValue, viewModel.pin)

        viewModel.userInputPIN(testValue)

        assertEquals("", viewModel.pin)
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }
    }

    @Test
    fun emoji() {
        val emoji = String(intArrayOf(0x274C), 0, 1)
        val testValue = "$emoji$emoji$emoji$emoji$emoji$emoji"

        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        assertEquals(defaultValue, viewModel.pin)

        viewModel.userInputPIN(testValue)

        assertEquals("", viewModel.pin)
        verify(exactly = 0) { mockCoordinator.onPersonalPinInput(testValue) }
    }

    @Test
    fun onNavigationButtonTapped() {
        val viewModel = SetupPersonalPinInputViewModel(mockCoordinator)

        viewModel.onNavigationButtonTapped()

        verify(exactly = 1) { mockCoordinator.onBackTapped() }
    }
}
