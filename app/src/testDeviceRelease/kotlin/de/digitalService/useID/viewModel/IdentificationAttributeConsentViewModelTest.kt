package de.digitalService.useID.viewModel

import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.digitalService.useID.idCardInterface.EidAttribute
import de.digitalService.useID.idCardInterface.IdentificationAttributes
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
        val testEidAttributes = listOf(
            EidAttribute.DG01,
            EidAttribute.DG02,
            EidAttribute.DG04
        )

        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockIdentificationAttributes: IdentificationAttributes = mockk(relaxed = true)
        val mockCertificateDescription: CertificateDescription = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationAttributes } returns mockIdentificationAttributes
        every { mockNavArgs.backAllowed } returns true
        every { mockIdentificationAttributes.requiredAttributes } returns testEidAttributes
        every { mockIdentificationAttributes.certificateDescription } returns mockCertificateDescription
        every { mockCertificateDescription.subjectName } returns testIdentificationProvider

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
        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockIdentificationAttributes: IdentificationAttributes = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationAttributes } returns mockIdentificationAttributes
        val identificationCoordinatorSetupSkipped = true
        every { mockNavArgs.backAllowed } returns identificationCoordinatorSetupSkipped

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify(exactly = 1) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 0) { mockIdentificationCoordinator.cancelIdentification() }
    }

    @Test
    fun testOnNavigationButtonClickedWithSetup() {
        val mockNavArgs: IdentificationAttributeConsentNavArgs = mockk()
        val mockIdentificationAttributes: IdentificationAttributes = mockk(relaxed = true)

        mockkObject(IdentificationAttributeConsentDestination)
        every { IdentificationAttributeConsentDestination.argsFrom(mockSaveStateHandle) } returns mockNavArgs
        every { mockNavArgs.identificationAttributes } returns mockIdentificationAttributes
        val identificationCoordinatorSetupSkipped = false
        every { mockNavArgs.backAllowed } returns identificationCoordinatorSetupSkipped

        val viewModel = IdentificationAttributeConsentViewModel(
            mockIdentificationCoordinator,
            mockSaveStateHandle
        )

        viewModel.onNavigationButtonClicked()

        verify(exactly = 0) { mockIdentificationCoordinator.onBack() }
        verify(exactly = 1) { mockIdentificationCoordinator.cancelIdentification() }
    }
}
