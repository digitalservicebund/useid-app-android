package de.digitalService.useID.ui.screens.setup

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
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText
import javax.inject.Inject

@Destination
@Composable
fun AlreadySetupConfirmation(viewModel: AlreadySetupConfirmationScreenViewModelInterface = hiltViewModel<AlreadySetupConfirmationScreenViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBackButtonClicked,
            confirmation = null
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_alreadySetupConfirmation_close),
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
                    stringResource(id = R.string.firstTimeUser_alreadySetupConfirmation_title),
                    style = UseIdTheme.typography.headingXl,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Card(
                    colors = CardDefaults.cardColors(containerColor = UseIdTheme.colors.green100),
                    shape = UseIdTheme.shapes.roundedMedium
                ) {
                    Column(modifier = Modifier.padding(UseIdTheme.spaces.s)) {
                        MarkdownText(
                            markdown = markDownResource(id = R.string.firstTimeUser_alreadySetupConfirmation_box),
                            fontResource = R.font.bundes_sans_dtp_regular,
                            fontSize = UseIdTheme.typography.bodyLRegular.fontSize,
                            color = UseIdTheme.colors.black
                        )
                    }
                }
            }
        }
    }
}

interface AlreadySetupConfirmationScreenViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class AlreadySetupConfirmationScreenViewModel @Inject constructor(private val coordinator: SetupCoordinator) :
    ViewModel(),
    AlreadySetupConfirmationScreenViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.confirmAlreadySetUp()
    override fun onBackButtonClicked() = coordinator.onBackClicked()
}

//region Preview
private class PreviewAlreadySetupConfirmationScreenViewModel : AlreadySetupConfirmationScreenViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewSetupAlreadyCompletedScreen() {
    UseIdTheme {
        AlreadySetupConfirmation(PreviewAlreadySetupConfirmationScreenViewModel())
    }
}
//endregion
