package de.digitalService.useID.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun BundInformationButton(onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = UseIdTheme.colors.blue200,
            contentColor = UseIdTheme.colors.blue800
        ),
        shape = UseIdTheme.shapes.roundedSmall,
        contentPadding = PaddingValues(),
        modifier = modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
    ) {
        Text(
            text = label,
            style = UseIdTheme.typography.bodyLBold,
            modifier = Modifier
                .padding(10.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewBundInformationButton() {
    UseIdTheme {
        BundInformationButton(onClick = { }, label = "More Information")
    }
}
