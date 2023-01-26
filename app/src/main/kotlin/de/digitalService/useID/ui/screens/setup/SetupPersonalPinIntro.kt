package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinIntro(viewModel: SetupPersonalPinIntroViewModelInterface = hiltViewModel<SetupPersonalPinIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBackButtonClicked,
            confirmation = null
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_personalPINIntro_continue),
                action = viewModel::onContinue
            ),
            modifier = Modifier.padding(top = topPadding)
        ) { bottomPadding ->
            Column(
                modifier = Modifier
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .padding(bottom = bottomPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(id = R.string.firstTimeUser_personalPINIntro_title),
                    style = UseIdTheme.typography.headingXl
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                BundCard(
                    type = BundCardType.INFO,
                    title = stringResource(R.string.firstTimeUser_personalPINIntro_info_title),
                    body = stringResource(id = R.string.firstTimeUser_personalPINIntro_info_body)
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                val imageId = R.drawable.eid_3_pin
                Image(
                    painter = painterResource(id = imageId),
                    contentScale = ContentScale.Inside,
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .semantics {
                            testTag = imageId.toString()
                        }
                )
            }
        }
    }
}

interface SetupPersonalPinIntroViewModelInterface {
    fun onContinue()
    fun onBackButtonClicked()
}

@HiltViewModel
class SetupPersonalPinIntroViewModel @Inject constructor(private val coordinator: PinManagementCoordinator) :
    ViewModel(),
    SetupPersonalPinIntroViewModelInterface {
    override fun onContinue() = coordinator.onPersonalPinIntroFinished()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewSetupPersonalPinIntroViewModel : SetupPersonalPinIntroViewModelInterface {
    override fun onContinue() { }
    override fun onBackButtonClicked() {}
}

@Preview
@Composable
fun PreviewSetupPersonalPinIntro() {
    UseIdTheme {
        SetupPersonalPinIntro(PreviewSetupPersonalPinIntroViewModel())
    }
}
//endregion
