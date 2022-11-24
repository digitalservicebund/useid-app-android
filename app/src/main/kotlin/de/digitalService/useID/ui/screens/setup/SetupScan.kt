package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Destination
@Composable
fun SetupScan(
    modifier: Modifier = Modifier,
    viewModel: SetupScanViewModelInterface = hiltViewModel<SetupScanViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onCancelConfirm)
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
}

interface SetupScanViewModelInterface {
    val shouldShowProgress: Boolean
    fun onHelpButtonTapped()
    fun onNfcButtonTapped()
    fun onCancelConfirm()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    private val trackerManager: TrackerManagerType,
    @Nullable coroutineScope: CoroutineScope? = null
) : ViewModel(), SetupScanViewModelInterface {

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope

    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    init {
        viewModelCoroutineScope.launch {
            coordinator.scanInProgress.collect { shouldShowProgress = it }
        }
    }

    override fun onHelpButtonTapped() {
        trackerManager.trackScreen("firstTimeUser/scanHelp")
    }

    override fun onNfcButtonTapped() {
        trackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo")
    }

    override fun onCancelConfirm() {
        coordinator.onBackTapped()
    }
}

class PreviewSetupScanViewModel(
    override val shouldShowProgress: Boolean
) :
    SetupScanViewModelInterface {
    override fun onHelpButtonTapped() {}
    override fun onNfcButtonTapped() {}
    override fun onCancelConfirm() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIDTheme {
        SetupScan(viewModel = PreviewSetupScanViewModel(false))
    }
}
