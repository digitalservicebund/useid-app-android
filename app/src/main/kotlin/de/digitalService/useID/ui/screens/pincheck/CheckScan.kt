package de.digitalService.useID.ui.screens.pincheck

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
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
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.screens.destinations.CheckScanDestination
import de.digitalService.useID.ui.screens.destinations.SetupScanDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Destination(navArgsDelegate = CheckScanNavArgs::class)
@Composable
fun CheckScan(
    modifier: Modifier = Modifier,
    viewModel: CheckScanViewModelInterface = hiltViewModel<CheckScanViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation =  Flow.Setup.takeIf { !viewModel.backAllowed }
        )
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.firstTimeUser_scan_title_android),
            body = stringResource(id = R.string.firstTimeUser_scan_body),
            onHelpDialogShown = viewModel::onHelpButtonClicked,
            onNfcDialogShown = viewModel::onNfcButtonClicked,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }
}

data class CheckScanNavArgs(
    val backAllowed: Boolean
)

interface CheckScanViewModelInterface {
    val backAllowed: Boolean
    val shouldShowProgress: Boolean
    fun onHelpButtonClicked()
    fun onNfcButtonClicked()
    fun onNavigationButtonClicked()
}

@HiltViewModel
class CheckScanViewModel @Inject constructor(
    private val checkPinCoordinator: CheckPinCoordinator,
    private val trackerManager: TrackerManagerType,
    savedStateHandle: SavedStateHandle,
    @Nullable coroutineScope: CoroutineScope? = null
) : ViewModel(), CheckScanViewModelInterface {

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope

    override val backAllowed: Boolean
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    init {
        backAllowed = CheckScanDestination.argsFrom(savedStateHandle).backAllowed

        viewModelCoroutineScope.launch {
            checkPinCoordinator.scanInProgress.collect { shouldShowProgress = it }
        }
    }

    override fun onHelpButtonClicked() {
        trackerManager.trackScreen("firstTimeUser/scanHelp")
    }

    override fun onNfcButtonClicked() {
        trackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo")
    }

    override fun onNavigationButtonClicked() {
        if (backAllowed) {
            checkPinCoordinator.onBack()
        } else {
            checkPinCoordinator.cancelPinCheck()
        }
    }
}

class PreviewCheckScanViewModel(
    override val backAllowed: Boolean,
    override val shouldShowProgress: Boolean
) :
    CheckScanViewModelInterface {
    override fun onHelpButtonClicked() {}
    override fun onNfcButtonClicked() {}
    override fun onNavigationButtonClicked() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIdTheme {
        CheckScan(viewModel = PreviewCheckScanViewModel(false, false))
    }
}
