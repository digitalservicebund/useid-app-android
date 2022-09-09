@file:OptIn(ExperimentalMaterial3Api::class)

package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.StandardButtonScreen
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.ui.theme.Yellow300
import javax.inject.Inject

@Destination
@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface = hiltViewModel<SetupFinishViewModel>()) {
    val buttonConfig = if (viewModel.identificationPending()) {
        BundButtonConfig(stringResource(id = R.string.firstTimeUser_done_identify), viewModel::onIdentifyButtonClicked)
    } else {
        BundButtonConfig(stringResource(id = R.string.firstTimeUser_done_close), viewModel::onCloseButtonClicked)
    }

    StandardStaticComposition(
        title = stringResource(id = R.string.firstTimeUser_done_title),
        body = null,
        imageID = R.drawable.eid_3_pin,
        imageScaling = ContentScale.Inside,
        primaryButton = buttonConfig
    )
}

interface SetupFinishViewModelInterface {
    fun onCloseButtonClicked()
    fun identificationPending(): Boolean
    fun onIdentifyButtonClicked()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupFinishViewModelInterface {
    override fun identificationPending(): Boolean {
        return setupCoordinator.identificationPending()
    }

    override fun onCloseButtonClicked() {
        setupCoordinator.onBackToHome()
    }

    override fun onIdentifyButtonClicked() {
        setupCoordinator.onSetupFinished()
    }
}

class PreviewSetupFinishViewModel(private val hasTcTokenUrl: Boolean) : SetupFinishViewModelInterface {
    override fun onCloseButtonClicked() {}
    override fun identificationPending(): Boolean = hasTcTokenUrl
    override fun onIdentifyButtonClicked() {}
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
