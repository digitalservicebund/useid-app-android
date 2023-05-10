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

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Text(
                    text = "Nutzen Sie die Online Ausweisfunktion mit einem Service.",
                    style = UseIdTheme.typography.bodyLRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onConfirmationButtonClicked,
                    label = "Selbstauskunft",
                    modifier = Modifier
                        .padding(UseIdTheme.spaces.s)
                )

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onConfirmationButtonClicked,
                    label = "Flensburg",
                    modifier = Modifier
                        .padding(UseIdTheme.spaces.s)
                )

                BundButton(
                    type = ButtonType.SECONDARY,
                    onClick = viewModel::onConfirmationButtonClicked,
                    label = "Rentenkonto",
                    modifier = Modifier
                        .padding(UseIdTheme.spaces.s)
                )
            }
        }
    }
}

interface CheckSuccessViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class CheckSuccessViewModel @Inject constructor(private val coordinator: CheckPinCoordinator) :
    ViewModel(),
    CheckSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.onFinish()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewCheckSuccessScreenViewModel : CheckSuccessViewModelInterface {
    override fun onConfirmationButtonClicked() {}
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
