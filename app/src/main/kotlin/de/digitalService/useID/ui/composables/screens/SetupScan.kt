package de.digitalService.useID.ui.composables.screens

import android.content.Context
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.composables.AppNavHost
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.SetupScanCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SetupScan(viewModel: SetupScanViewModelInterface, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, animationSpec = TweenSpec(200))

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            ScreenWithTopBar(navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.navigation_cancel)
                    )
                }
                }) { topPadding ->
                SetupTransportPIN(
                    viewModel = SetupTransportPINViewModel(attempts = viewModel.attempts, onDone = { viewModel.onReEnteredTransportPIN(it, context) }),
                    modifier = Modifier.padding(top = topPadding)
                )
            }
        }) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
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
                onClick = viewModel::onHelpButtonTapped,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(40.dp)
            ) {
                Text(stringResource(id = R.string.firstTimeUser_scan_helpButton))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onUIInitialized(context)
    }
}

interface SetupScanViewModelInterface {
    val attempts: Int

    fun onUIInitialized(context: Context)
    fun onReEnteredTransportPIN(newTransportPIN: String, context: Context)
    fun onHelpButtonTapped()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: SetupScanCoordinator,
    private val idCardManager: IDCardManager,
    @Nullable private val coroutineScope: CoroutineScope? = null,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupScanViewModelInterface {
    private val logger by getLogger()

    private val transportPIN: String
    private val personalPIN: String

    private val viewModelCoroutineScope: CoroutineScope

    override var attempts: Int by mutableStateOf(3)
        private set

    init {
        transportPIN = Screen.SetupScan.transportPIN(savedStateHandle)
        personalPIN = Screen.SetupScan.personalPIN(savedStateHandle)

        viewModelCoroutineScope = coroutineScope ?: viewModelScope
    }

    override fun onUIInitialized(context: Context) {
        executePINManagement(transportPIN, personalPIN, context)
    }

    override fun onReEnteredTransportPIN(newTransportPIN: String, context: Context) {
        executePINManagement(newTransportPIN, personalPIN, context)
    }

    override fun onHelpButtonTapped() {}

    private fun executePINManagement(oldPIN: String, newPIN: String, context: Context) {
        viewModelCoroutineScope.launch {
            idCardManager.changePin(context).collect { event ->
                when (event) {
                    EIDInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EIDInteractionEvent.CardRecognized -> logger.debug("Card recognized.")
                    EIDInteractionEvent.CardRemoved -> logger.debug("Card removed.")
                    EIDInteractionEvent.PINManagementStarted -> {
                        logger.debug("PIN management started.")
                    }
                    EIDInteractionEvent.ProcessCompletedSuccessfully -> {
                        logger.debug("Process completed.")
                        coordinator.settingPINSucceeded()
                    }
                    EIDInteractionEvent.RequestCardInsertion -> logger.debug("Insert card.")
                    is EIDInteractionEvent.RequestChangedPIN -> {
                        logger.debug("Changed PIN requested. Entering transport PIN and personal PIN")
                        event.attempts?.let { attempts = it }
                        event.pinCallback(oldPIN, newPIN)
                    }
                    else -> logger.debug("Collected unexpected event: $event")
                }
            }
        }
    }
}

class PreviewSetupScanViewModel(override val attempts: Int) : SetupScanViewModelInterface {
    override fun onUIInitialized(context: Context) {}
    override fun onReEnteredTransportPIN(newTransportPIN: String, context: Context) { }
    override fun onHelpButtonTapped() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScan() {
    UseIDTheme {
        SetupScan(PreviewSetupScanViewModel(3))
    }
}
