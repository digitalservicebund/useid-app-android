package de.digitalService.useID.ui.screens.identification

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.launch
import javax.inject.Inject

@Destination
@Composable
fun IdentificationScan(
    modifier: Modifier = Modifier,
    viewModel: IdentificationScanViewModelInterface = hiltViewModel<IdentificationScanViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            onClick = viewModel::onCancelIdentification,
            shouldShowConfirmDialog = true,
            isIdentification = true
        )
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.identification_scan_title),
            body = stringResource(id = R.string.identification_scan_body),
            onHelpDialogShown = viewModel::onHelpButtonTapped,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }
}

sealed class ScanEvent {
    object CardRequested : ScanEvent()
    object CardAttached : ScanEvent()
    data class Finished(val redirectAddress: String) : ScanEvent()
    data class Error(val error: ScanError) : ScanEvent()
}

interface IdentificationScanViewModelInterface {
    val shouldShowProgress: Boolean
    val errorState: ScanError.IncorrectPIN?

    fun onHelpButtonTapped()
    fun onNfcButtonTapped()
    fun onErrorDialogButtonTapped(context: Context)
    fun onCancelIdentification()
    fun onNewPersonalPINEntered(pin: String)
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordinator: IdentificationCoordinator,
    private val coroutineContextProvider: CoroutineContextProviderType,
    private val trackerManager: TrackerManagerType
) : ViewModel(), IdentificationScanViewModelInterface {
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    override var errorState: ScanError.IncorrectPIN? by mutableStateOf(null)
        private set

    init {
        collectScanEvents()
    }

    override fun onHelpButtonTapped() {
        trackerManager.trackScreen("identification/scanHelp")
    }

    override fun onNfcButtonTapped() {
        trackerManager.trackEvent("identification", "alertShown", "NFCInfo")
    }

    override fun onErrorDialogButtonTapped(context: Context) {
        coordinator.cancelIdentification()
    }

    override fun onCancelIdentification() {
        coordinator.cancelIdentification()
    }

    override fun onNewPersonalPINEntered(pin: String) {
        errorState = null
        coordinator.onPINEntered(pin)
    }

    private fun collectScanEvents() {
        viewModelScope.launch(coroutineContextProvider.IO) {
            coordinator.scanEventFlow.collect { event: ScanEvent ->
                launch(coroutineContextProvider.Main) {
                    when (event) {
                        ScanEvent.CardRequested -> shouldShowProgress = false
                        ScanEvent.CardAttached -> shouldShowProgress = true
                        is ScanEvent.Finished -> {
                            shouldShowProgress = false

                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.redirectAddress))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(context, intent, null)
                        }
                        is ScanEvent.Error -> {
                            shouldShowProgress = false

                            if (event.error is ScanError.IncorrectPIN) {
                                errorState = event.error
                            }
                        }
                    }
                }
            }
        }
    }
}

private class PreviewIdentificationScanViewModel(
    override val shouldShowProgress: Boolean,
    override val errorState: ScanError.IncorrectPIN?
) : IdentificationScanViewModelInterface {
    override fun onHelpButtonTapped() {}
    override fun onNfcButtonTapped() {}
    override fun onErrorDialogButtonTapped(context: Context) {}
    override fun onCancelIdentification() {}
    override fun onNewPersonalPINEntered(pin: String) {}
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScan() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false, null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithProgress() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithError() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, ScanError.IncorrectPIN(2)))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithCancelDialog() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false, null))
    }
}
