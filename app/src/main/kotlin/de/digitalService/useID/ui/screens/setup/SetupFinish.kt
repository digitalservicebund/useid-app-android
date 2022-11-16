package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface = hiltViewModel<SetupFinishViewModel>()) {
    val buttonLabelStringId = if (viewModel.identificationPending()) {
        stringResource(id = R.string.firstTimeUser_done_identify)
    } else {
        stringResource(id = R.string.firstTimeUser_done_close)
    }

    val buttonConfig = BundButtonConfig(buttonLabelStringId, viewModel::onButtonTapped)

    val navigationButton = NavigationButton(NavigationIcon.Cancel, onClick = viewModel::onButtonTapped)
        .takeIf { !viewModel.identificationPending() }

    ScreenWithTopBar(navigationButton = navigationButton) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_done_title),
            body = null,
            imageID = R.drawable.eid_3_pin,
            imageScaling = ContentScale.Inside,
            primaryButton = buttonConfig,
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface SetupFinishViewModelInterface {
    fun identificationPending(): Boolean
    fun onButtonTapped()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupFinishViewModelInterface {
    override fun identificationPending(): Boolean {
        return setupCoordinator.identificationPending()
    }

    override fun onButtonTapped() {
        setupCoordinator.onSetupFinished()
    }
}

class PreviewSetupFinishViewModel(private val hasTcTokenUrl: Boolean) : SetupFinishViewModelInterface {
    override fun identificationPending(): Boolean = hasTcTokenUrl
    override fun onButtonTapped() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewHasToken() {
    UseIDTheme {
        SetupFinish(PreviewSetupFinishViewModel(true))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHasNoToken() {
    UseIDTheme {
        SetupFinish(PreviewSetupFinishViewModel(false))
    }
}
