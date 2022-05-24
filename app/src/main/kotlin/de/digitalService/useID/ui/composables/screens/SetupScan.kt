package de.digitalService.useID.ui.composables.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun SetupScan(viewModel: SetupScanViewModelInterface) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.eids),
            contentScale = ContentScale.Fit,
            contentDescription = ""
        )
        Text(
            stringResource(id = R.string.firstTimeUser_scan_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(id = R.string.firstTimeUser_scan_body),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = viewModel::onHelpButtonTapped, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ), shape = MaterialTheme.shapes.small, modifier = Modifier
                .height(40.dp)
        ) {
            Text(stringResource(id = R.string.firstTimeUser_scan_helpButton))
        }
    }
}

interface SetupScanViewModelInterface {
    fun onHelpButtonTapped()
}

class SetupScanViewModel(
    val navController: NavController,
    val transportPIN: String,
    val personalPIN: String
) :
    ViewModel(), SetupScanViewModelInterface {

    override fun onHelpButtonTapped() {}
}

class PreviewSetupScanViewModel : SetupScanViewModelInterface {
    override fun onHelpButtonTapped() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScan() {
    UseIDTheme {
        SetupScan(PreviewSetupScanViewModel())
    }
}