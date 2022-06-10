package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Device
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun IdentificationAttributeConsent(viewModel: IdentificationAttributeConsentViewModelInterface) {
    Column {
        Text(
            viewModel.identificationProvider,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "ABC",
            style = MaterialTheme.typography.bodySmall
        )
        Surface(
            shape = RoundedCornerShape(5.dp),
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
        ) {
            Text("ABC")
        }
    }
}

interface IdentificationAttributeConsentViewModelInterface {
    val identificationProvider: String
}

class PreviewIdentificationAttributeConsentViewModel(override val identificationProvider: String) :
    IdentificationAttributeConsentViewModelInterface

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationAttributeConsent() {
    UseIDTheme {
        IdentificationAttributeConsent(viewModel = PreviewIdentificationAttributeConsentViewModel("Grundsteuer"))
    }
}
