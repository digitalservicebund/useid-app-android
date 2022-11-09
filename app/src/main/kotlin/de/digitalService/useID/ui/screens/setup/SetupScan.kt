package de.digitalService.useID.ui.screens.setup

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Destination
@Composable
fun SetupScan(
    modifier: Modifier = Modifier,
    viewModel: SetupScanViewModelInterface = hiltViewModel<SetupScanViewModel>()
) {
    val context = LocalContext.current

    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBackButtonTapped)
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.firstTimeUser_scan_title),
            body = stringResource(id = R.string.firstTimeUser_scan_body),
            onHelpDialogShown = viewModel::onHelpButtonTapped,
            onNfcDialogShown = viewModel::onNfcButtonTapped,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.startSettingPIN(context)
    }
}

interface SetupScanViewModelInterface {
    val shouldShowProgress: Boolean

    fun startSettingPIN(context: Context)
    fun onReEnteredTransportPIN(transportPIN: String, context: Context)
    fun onHelpButtonTapped()
    fun onNfcButtonTapped()
    fun onBackButtonTapped()
    fun onCancelConfirm()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    private val idCardManager: IDCardManager,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType,
    @Nullable coroutineScope: CoroutineScope? = null
) : ViewModel(), SetupScanViewModelInterface {
    private val logger by getLogger()

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope
    private var firstPINCallback: Boolean = true

    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    private var cancelled = false

    override fun startSettingPIN(context: Context) {
        val transportPIN = coordinator.transportPin ?: run {
            logger.error("Transport PIN not available.")

            coordinator.onScanError(ScanError.Other(null))
            return
        }
        executePINManagement(transportPIN, context)
    }

    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {
        firstPINCallback = true
        executePINManagement(transportPIN, context)
    }

    override fun onHelpButtonTapped() {
        trackerManager.trackScreen("firstTimeUser/scanHelp")
    }

    override fun onNfcButtonTapped() {
        trackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo")
    }

    override fun onBackButtonTapped() {
        cancelled = true
        idCardManager.cancelTask()
        coordinator.onBackTapped()
    }

    override fun onCancelConfirm() {
        cancelled = true
        idCardManager.cancelTask()
        coordinator.cancelSetup()
    }

    private fun finishSetup() {
        coordinator.onSettingPINSucceeded()
    }

    private fun executePINManagement(transportPIN: String, context: Context) {
        val newPIN = coordinator.personalPin ?: run {
            logger.error("Personal PIN not available.")
            coordinator.onScanError(ScanError.Other(null))
            return
        }

        cancelled = false
        firstPINCallback = true

        viewModelCoroutineScope.launch {
            logger.debug("Starting PIN management flow.")
            idCardManager.changePin(context).catch { exception ->
                if (cancelled) {
                    return@catch
                }

                when (exception) {
                    is IDCardInteractionException.CardDeactivated -> {
                        trackerManager.trackScreen("firstTimeUser/cardDeactivated")

                        coordinator.onScanError(ScanError.CardDeactivated)
                    }
                    is IDCardInteractionException.CardBlocked -> {
                        coordinator.onScanError(ScanError.PINBlocked)
                    }
                    else -> {
                        trackerManager.trackScreen("firstTimeUser/cardUnreadable")

                        (exception as? IDCardInteractionException)?.redacted?.let {
                            issueTrackerManager.capture(it)
                        }

                        coordinator.onScanError(ScanError.Other(exception.message))
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EIDInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        shouldShowProgress = true
                    }
                    EIDInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        shouldShowProgress = false
                    }
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started.")
                    EIDInteractionEvent.PINManagementStarted -> {
                        logger.debug("PIN management started.")
                    }
                    is EIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        finishSetup()
                    }
                    EIDInteractionEvent.RequestCardInsertion -> logger.debug("Card insertion requested.")
                    is EIDInteractionEvent.RequestChangedPIN -> {
                        if (firstPINCallback) {
                            logger.debug("Changed PIN requested. Entering transport PIN and personal PIN")
                            event.pinCallback(transportPIN, newPIN)
                            firstPINCallback = false
                        } else {
                            logger.debug("Old and new PIN requested for a second time. The Transport-PIN seems to be incorrect.")
                            val attempts = event.attempts ?: run {
                                logger.error("Old and new PIN requested without attempts.")
                                cancel()
                                return@collect
                            }
                            shouldShowProgress = false
                            cancelled = true

                            coordinator.onIncorrectTransportPIN(attempts)

                            trackerManager.trackScreen("firstTimeUser/incorrectTransportPIN")
                            cancel()
                        }
                    }
                    is EIDInteractionEvent.RequestCANAndChangedPIN -> {
                        coordinator.onScanError(ScanError.PINSuspended)

                        trackerManager.trackScreen("firstTimeUser/cardSuspended")
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPUK -> {
                        coordinator.onScanError(ScanError.PINBlocked)

                        trackerManager.trackScreen("firstTimeUser/cardBlocked")
                        cancel()
                    }
                    else -> {
                        logger.debug("Collected unexpected event: $event")
                        coordinator.onScanError(ScanError.Other(null))

                        issueTrackerManager.capture(event.redacted)
                        cancel()
                    }
                }
            }
        }
    }
}

class PreviewSetupScanViewModel(
    override val shouldShowProgress: Boolean
) :
    SetupScanViewModelInterface {
    override fun startSettingPIN(context: Context) {}
    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {}
    override fun onHelpButtonTapped() {}
    override fun onNfcButtonTapped() {}
    override fun onBackButtonTapped() {}
    override fun onCancelConfirm() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIDTheme {
        SetupScan(viewModel = PreviewSetupScanViewModel(false))
    }
}
