package de.digitalService.useID.ui.screens.pincheck

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButton
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.ButtonType
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardButtonScreen
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun CheckSuccess(viewModel: CheckSuccessViewModelInterface = hiltViewModel<CheckSuccessViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBackButtonClicked,
            confirmation = null
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.scanError_close),
                action = viewModel::onConfirmationButtonClicked
            ),
            modifier = Modifier.padding(top = topPadding)
        ) { bottomPadding ->
            Column(
                modifier = Modifier
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .padding(bottom = bottomPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_success),
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(UseIdTheme.spaces.m)
                )

                Text(
                    text = "Test erfolgreich",
                    style = UseIdTheme.typography.headingXl,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.xxxl))

                Text(
                    text = "Nutzen Sie die Online Ausweisfunktion mit einem Service.",
                    style = UseIdTheme.typography.bodyLBold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onSelbstAuskunftClicked,
                    label = "Selbstauskunft",
                    modifier = Modifier
                        .padding(vertical = UseIdTheme.spaces.s)
                )

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onFlensburgClicked,
                    label = "Flensburg",
                    modifier = Modifier
                        .padding(vertical = UseIdTheme.spaces.s)
                )

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onRenteClicked,
                    label = "Rentenkonto",
                    modifier = Modifier
                        .padding(vertical = UseIdTheme.spaces.s)
                )
            }
        }
    }
}

interface CheckSuccessViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onSelbstAuskunftClicked()
    fun onFlensburgClicked()
    fun onRenteClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class CheckSuccessViewModel @Inject constructor(private val coordinator: CheckPinCoordinator) :
    ViewModel(),
    CheckSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.onFinish()
    override fun onSelbstAuskunftClicked() = coordinator.selbstauskunft()

    override fun onFlensburgClicked() = coordinator.flensburg()

    override fun onRenteClicked() = coordinator.rente()

    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewCheckSuccessScreenViewModel : CheckSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onSelbstAuskunftClicked() {}

    override fun onFlensburgClicked() {}

    override fun onRenteClicked() {}

    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewCheckSuccessScreen() {
    UseIdTheme {
        CheckSuccess(PreviewCheckSuccessScreenViewModel())
    }
}
//endregion
