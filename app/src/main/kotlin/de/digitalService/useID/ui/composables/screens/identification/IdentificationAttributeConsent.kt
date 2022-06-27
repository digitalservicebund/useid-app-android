package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Device
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.theme.UseIDTheme
import org.openecard.bouncycastle.math.raw.Mod
import javax.inject.Inject

@Destination(
    navArgsDelegate = IdentificationAttributeConsentNavArgs::class
)
@Composable
fun IdentificationAttributeConsent(
    modifier: Modifier = Modifier,
    viewModel: IdentificationAttributeConsentViewModelInterface = hiltViewModel<IdentificationAttributeConsentViewModel>()
) {
    Column(
        modifier = modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            viewModel.identificationProvider,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            stringResource(
                id = R.string.identification_attributeConsent_body,
                viewModel.identificationProvider
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.secondary,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(start = 40.dp, top = 20.dp)) {
                viewModel.requiredReadAttributes.forEach { attributeId ->
                    Text(
                        "\u2022 ${stringResource(id = attributeId)}",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(20.dp))
        BundButton(
            type = ButtonType.PRIMARY,
            onClick = { },
            label = stringResource(id = R.string.identification_attributeConsent_pinButton)
        )
    }
}

data class IdentificationAttributeConsentNavArgs(
    val request: EIDAuthenticationRequest
)

interface IdentificationAttributeConsentViewModelInterface {
    val identificationProvider: String
    val requiredReadAttributes: List<Int>
}

@HiltViewModel
class IdentificationAttributeConsentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationAttributeConsentViewModelInterface {
    override val identificationProvider: String
    override val requiredReadAttributes: List<Int>

    init {
        val request = IdentificationAttributeConsentDestination.argsFrom(savedStateHandle).request
        identificationProvider = request.subject
        requiredReadAttributes = request
            .readAttributes
            .filterValues { it }
            .keys
            .map { attributeDescriptionID(it) }
    }

    private fun attributeDescriptionID(attribute: IDCardAttribute): Int = when (attribute) {
        IDCardAttribute.DG01 -> R.string.idCardAttribute_DG01
        IDCardAttribute.DG02 -> R.string.idCardAttribute_DG02
        IDCardAttribute.DG03 -> R.string.idCardAttribute_DG03
        IDCardAttribute.DG04 -> R.string.idCardAttribute_DG04
        IDCardAttribute.DG05 -> R.string.idCardAttribute_DG05
        IDCardAttribute.DG06 -> R.string.idCardAttribute_DG06
        IDCardAttribute.DG07 -> R.string.idCardAttribute_DG07
        IDCardAttribute.DG08 -> R.string.idCardAttribute_DG08
        IDCardAttribute.DG09 -> R.string.idCardAttribute_DG09
        IDCardAttribute.DG10 -> R.string.idCardAttribute_DG10
        IDCardAttribute.DG13 -> R.string.idCardAttribute_DG13
        IDCardAttribute.DG17 -> R.string.idCardAttribute_DG17
        IDCardAttribute.DG19 -> R.string.idCardAttribute_DG19
        IDCardAttribute.RESTRICTED_IDENTIFICATION -> R.string.idCardAttribute_restrictedIdentification
        IDCardAttribute.AGE_VERIFICATION -> R.string.idCardAttribute_ageVerification
    }
}

class PreviewIdentificationAttributeConsentViewModel(
    override val identificationProvider: String,
    override val requiredReadAttributes: List<Int>
) :
    IdentificationAttributeConsentViewModelInterface

private val previewIdentificationAttributeConsentViewModel =
    PreviewIdentificationAttributeConsentViewModel(
        identificationProvider = "Grundsteuer",
        requiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG03,
            R.string.idCardAttribute_DG04,
            R.string.idCardAttribute_DG05,
        )
    )

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationAttributeConsent() {
    UseIDTheme {
        IdentificationAttributeConsent(viewModel = previewIdentificationAttributeConsentViewModel)
    }
}
