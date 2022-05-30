package de.digitalService.useID.ui.composables.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.coordinators.PersonalPINCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SetupScan(viewModel: SetupScanViewModelInterface) {
    val context = LocalContext.current

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

    LaunchedEffect(Unit) {
        viewModel.onUIInitialized(context)
    }
}

interface SetupScanViewModelInterface {
    fun onUIInitialized(context: Context)
    fun onHelpButtonTapped()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: PersonalPINCoordinator,
    private val idCardManager: IDCardManager,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupScanViewModelInterface {

    val transportPIN: String
    val personalPIN: String

    init {
        transportPIN = Screen.SetupScan.transportPIN(savedStateHandle)
        personalPIN = Screen.SetupScan.personalPIN(savedStateHandle)
    }

    override fun onUIInitialized(context: Context) {
        viewModelScope.launch {
            idCardManager.changePin(context).collect { event ->
                when(event) {
                    EIDInteractionEvent.CardInteractionComplete -> Log.d("DEBUG", "Card interaction complete.")
                    EIDInteractionEvent.CardRecognized -> Log.d("DEBUG", "Card recognized.")
                    EIDInteractionEvent.CardRemoved -> Log.d("DEBUG", "Card removed.")
                    EIDInteractionEvent.PINManagementStarted -> Log.d("DEBUG", "PIN management started.")
                    EIDInteractionEvent.ProcessCompletedSuccessfully -> Log.d("DEBUG", "Process completed.")
                    EIDInteractionEvent.RequestCardInsertion -> Log.d("DEBUG", "Insert card.")
                    is EIDInteractionEvent.RequestChangedPIN -> {
//                        Log.d("DEBUG", "Changed PIN requested. Entering old PIN: $transportPIN, new PIN: $personalPIN")
//                        event.pinCallback(transportPIN, personalPIN)
                    }
                    else -> Log.d("DEBUG", "Collected unexpected event.")
                }
            }
        }
    }

    override fun onHelpButtonTapped() {}
}

class PreviewSetupScanViewModel : SetupScanViewModelInterface {
    override fun onUIInitialized(context: Context) {}
    override fun onHelpButtonTapped() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScan() {
    UseIDTheme {
        SetupScan(PreviewSetupScanViewModel())
    }
}