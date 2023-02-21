package de.digitalService.useID.ui.screens.setup

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
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.screens.destinations.SetupScanDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Destination(navArgsDelegate = SetupScanNavArgs::class)
@Composable
fun SetupScan(
    modifier: Modifier = Modifier,
    viewModel: SetupScanViewModelInterface = hiltViewModel<SetupScanViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = (if (viewModel.identificationPending) Flow.Identification else Flow.Setup).takeIf { !viewModel.backAllowed }
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

data class SetupScanNavArgs(
    val backAllowed: Boolean,
    val identificationPending: Boolean
)

interface SetupScanViewModelInterface {
    val backAllowed: Boolean
    val identificationPending: Boolean
    val shouldShowProgress: Boolean
    fun onHelpButtonClicked()
    fun onNfcButtonClicked()
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val pinManagementCoordinator: PinManagementCoordinator,
    private val trackerManager: TrackerManagerType,
    savedStateHandle: SavedStateHandle,
    @Nullable coroutineScope: CoroutineScope? = null
) : ViewModel(), SetupScanViewModelInterface {

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope

    override val backAllowed: Boolean
    override val identificationPending: Boolean
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    init {
        backAllowed = SetupScanDestination.argsFrom(savedStateHandle).backAllowed
        identificationPending = SetupScanDestination.argsFrom(savedStateHandle).identificationPending

        viewModelCoroutineScope.launch {
            pinManagementCoordinator.scanInProgress.collect { shouldShowProgress = it }
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
            pinManagementCoordinator.onBack()
        } else {
            pinManagementCoordinator.cancelPinManagement()
        }
    }
}

class PreviewSetupScanViewModel(
    override val backAllowed: Boolean,
    override val identificationPending: Boolean,
    override val shouldShowProgress: Boolean
) :
    SetupScanViewModelInterface {
    override fun onHelpButtonClicked() {}
    override fun onNfcButtonClicked() {}
    override fun onNavigationButtonClicked() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIdTheme {
        SetupScan(viewModel = PreviewSetupScanViewModel(false, false, false))
    }
}
