package de.digitalService.useID.ui.screens.pincheck

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText
import javax.inject.Inject

@Destination
@Composable
fun ScanSuccess(viewModel: ScanSuccessViewModelInterface = hiltViewModel<ScanSuccessViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            onClick = viewModel::onBackButtonClicked,
            confirmation = null
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = "PIN überprüfen",
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
                    painter = painterResource(id = R.drawable.icon_scan_success),
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(UseIdTheme.spaces.m)
                )

                Text(
                    text = "Das Scannen von Ihrem Ausweis hat funktioniert.",
                    style = UseIdTheme.typography.headingXl,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Text(
                    text = "Als nächstes wird ihr PIN geprüft.",
                    style = UseIdTheme.typography.bodyLRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )
            }
        }
    }
}

interface ScanSuccessViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class ScanSuccessViewModel @Inject constructor(private val coordinator: CheckPinCoordinator) :
    ViewModel(),
    ScanSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.onContinue()
    override fun onBackButtonClicked() = coordinator.cancelPinCheck()
}

//region Preview
private class PreviewScanSuccessScreenViewModel : ScanSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewSetupAlreadyCompletedScreen() {
    UseIdTheme {
        ScanSuccess(PreviewScanSuccessScreenViewModel())
    }
}
//endregion
