package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
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
            confirmation = Flow.Identification
        )
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.identification_scan_title_android),
            body = stringResource(id = R.string.identification_scan_body),
            onHelpDialogShown = viewModel::onHelpButtonClicked,
            onNfcDialogShown = viewModel::onNfcButtonClicked,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }
}

interface IdentificationScanViewModelInterface {
    val shouldShowProgress: Boolean

    fun onHelpButtonClicked()
    fun onNfcButtonClicked()
    fun onCancelIdentification()
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    private val trackerManager: TrackerManagerType,
    @Nullable coroutineScope: CoroutineScope? = null
) : ViewModel(), IdentificationScanViewModelInterface {
    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope

    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    init {
        viewModelCoroutineScope.launch {
            coordinator.scanInProgress.collect { shouldShowProgress = it }
        }
    }

    override fun onHelpButtonClicked() {
        trackerManager.trackScreen("identification/scanHelp")
    }

    override fun onNfcButtonClicked() {
        trackerManager.trackEvent("identification", "alertShown", "NFCInfo")
    }

    override fun onCancelIdentification() {
        coordinator.cancelIdentification()
    }
}

private class PreviewIdentificationScanViewModel(
    override val shouldShowProgress: Boolean
) : IdentificationScanViewModelInterface {
    override fun onHelpButtonClicked() {}
    override fun onNfcButtonClicked() {}
    override fun onCancelIdentification() {}
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScan() {
    UseIdTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithProgress() {
    UseIdTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true))
    }
}
