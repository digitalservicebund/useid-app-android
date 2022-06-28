package de.digitalService.useID

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.composables.screens.identification.IdentificationAttributeConsentNavArgs
import de.digitalService.useID.ui.composables.screens.identification.IdentificationAttributeConsentViewModel
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
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
            IDCardAttribute.DG01 to true,
            IDCardAttribute.DG02 to true,
            IDCardAttribute.DG03 to false,
            IDCardAttribute.DG04 to true,
            IDCardAttribute.DG05 to false,
        )

        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockRequest: EIDAuthenticationRequest = mockk(relaxed = true)

        every { mockNavArgs.request } returns mockRequest

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs

        every { mockRequest.subject } returns testIdentificationProvider
        every { mockRequest.readAttributes } returns testIdAttributes

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle,
        )

        val expectedRequiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG04,
        )
        Assertions.assertEquals(expectedRequiredReadAttributes, viewModel.requiredReadAttributes)
        Assertions.assertEquals(testIdentificationProvider, viewModel.identificationProvider)

        viewModel.onPINButtonTapped()

        verify(exactly = 1) { mockIdentificationCoordinator.confirmAttributesForIdentification() }
        verify(exactly = 1) { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) }

        Assertions.assertEquals(expectedRequiredReadAttributes, viewModel.requiredReadAttributes)
        Assertions.assertEquals(testIdentificationProvider, viewModel.identificationProvider)
    }

}
