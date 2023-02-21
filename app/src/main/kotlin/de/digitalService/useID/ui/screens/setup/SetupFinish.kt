package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupFinishDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = SetupFinishNavArgs::class)
@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface = hiltViewModel<SetupFinishViewModel>()) {
    val buttonLabelString = if (viewModel.identificationPending) {
        stringResource(id = R.string.firstTimeUser_done_identify)
    } else {
        stringResource(id = R.string.firstTimeUser_done_close)
    }

    val buttonConfig = BundButtonConfig(buttonLabelString, viewModel::onButtonClicked)

    val navigationButton = NavigationButton(
        icon = NavigationIcon.Cancel,
        onClick = viewModel::onButtonClicked,
        confirmation = Flow.Identification.takeIf { viewModel.identificationPending }
    ).takeIf { !viewModel.identificationPending }

    ScreenWithTopBar(navigationButton = navigationButton) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_done_title),
            body = null,
            imageId = R.drawable.eid_3_pin,
            imageScaling = ContentScale.Inside,
            primaryButton = buttonConfig,
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

data class SetupFinishNavArgs(
    val identificationPending: Boolean
)

interface SetupFinishViewModelInterface {
    val identificationPending: Boolean
    fun onButtonClicked()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), SetupFinishViewModelInterface {
    override val identificationPending: Boolean

    init {
        identificationPending = SetupFinishDestination.argsFrom(savedStateHandle).identificationPending
    }

    override fun onButtonClicked() {
        setupCoordinator.onSetupFinishConfirmed()
    }
}

class PreviewSetupFinishViewModel(private val hasTcTokenUrl: Boolean) : SetupFinishViewModelInterface {
    override val identificationPending: Boolean = hasTcTokenUrl
    override fun onButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewHasToken() {
    UseIdTheme {
        SetupFinish(PreviewSetupFinishViewModel(true))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHasNoToken() {
    UseIdTheme {
        SetupFinish(PreviewSetupFinishViewModel(false))
    }
}
