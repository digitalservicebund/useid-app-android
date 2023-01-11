package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.IdCardAttribute
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsentNavArgs
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsentViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationAttributeConsentViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSaveStateHandle: SavedStateHandle

    @Test
    fun success() {
        val testIdentificationProvider = "identificationProvider"
        val testIdAttributes = mapOf(
            IdCardAttribute.DG01 to true,
            IdCardAttribute.DG02 to true,
            IdCardAttribute.DG03 to false,
            IdCardAttribute.DG04 to true,
            IdCardAttribute.DG05 to false
        )

        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockRequest: EidAuthenticationRequest = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.request } returns mockRequest
        every { mockNavArgs.backAllowed} returns true
        every { mockRequest.subject } returns testIdentificationProvider
        every { mockRequest.readAttributes } returns testIdAttributes
        every { mockRequest.terms } returns AuthenticationTerms.Text("termsText")

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        val expectedRequiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg04
        )
        Assertions.assertEquals(expectedRequiredReadAttributes, viewModel.requiredReadAttributes)
        Assertions.assertEquals(testIdentificationProvider, viewModel.identificationProvider)

        viewModel.onPinButtonClicked()

        verify(exactly = 1) { mockIdentificationCoordinator.confirmAttributesForIdentification() }
        verify(exactly = 1) { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) }

        Assertions.assertEquals(expectedRequiredReadAttributes, viewModel.requiredReadAttributes)
        Assertions.assertEquals(testIdentificationProvider, viewModel.identificationProvider)
    }

    @Test
    fun testOnNavigationButtonClickedWithNoSetup() {
        val testIdentificationProvider = "identificationProvider"
        val testIdAttributes = mapOf(
            IdCardAttribute.DG01 to true,
            IdCardAttribute.DG02 to true,
            IdCardAttribute.DG03 to false,
            IdCardAttribute.DG04 to true,
            IdCardAttribute.DG05 to false
        )

        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockRequest: EidAuthenticationRequest = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.request } returns mockRequest
        val identificationCoordinatorSetupSkipped = true
        every { mockNavArgs.backAllowed} returns identificationCoordinatorSetupSkipped
        every { mockRequest.subject } returns testIdentificationProvider
        every { mockRequest.readAttributes } returns testIdAttributes
        every { mockRequest.terms } returns AuthenticationTerms.Text("termsText")

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify(exactly = 1) { mockIdentificationCoordinator.onBack() }
    }

    @Test
    fun testOnNavigationButtonClickedWithSetup() {
        val testIdentificationProvider = "identificationProvider"
        val testIdAttributes = mapOf(
            IdCardAttribute.DG01 to true,
            IdCardAttribute.DG02 to true,
            IdCardAttribute.DG03 to false,
            IdCardAttribute.DG04 to true,
            IdCardAttribute.DG05 to false
        )

        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockRequest: EidAuthenticationRequest = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.request } returns mockRequest
        val identificationCoordinatorSetupSkipped = false
        every { mockNavArgs.backAllowed} returns identificationCoordinatorSetupSkipped
        every { mockRequest.subject } returns testIdentificationProvider
        every { mockRequest.readAttributes } returns testIdAttributes
        every { mockRequest.terms } returns AuthenticationTerms.Text("termsText")

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
