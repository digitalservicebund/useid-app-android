package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SetupPersonalPinIntro(viewModel: SetupPersonalPinIntroViewModelInterface = hiltViewModel<SetupPersonalPinIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBackButtonClicked
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_personalPINIntro_continue),
                action = viewModel::onSetPin
            ),
            modifier = Modifier.padding(top = topPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(id = R.string.firstTimeUser_personalPINIntro_title),
                    style = UseIdTheme.typography.headingXl
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Card(
                    colors = CardDefaults.cardColors(containerColor = UseIdTheme.colors.blue200),
                    shape = UseIdTheme.shapes.roundedLarge
                ) {
                    Column(
                        modifier = Modifier
                            .padding(UseIdTheme.spaces.s)
                    ) {
                        val iconSize = 26.dp
                        val iconTextSpacerWidth = 6.dp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(iconSize),
                                tint = UseIdTheme.colors.blue700
                            )
                            Spacer(modifier = Modifier.width(iconTextSpacerWidth))
                            Text(
                                text = stringResource(R.string.firstTimeUser_personalPINIntro_info_title),
                                style = UseIdTheme.typography.bodyMBold,
                                color = UseIdTheme.colors.black
                            )
                        }

                        Spacer(modifier = Modifier.height(UseIdTheme.spaces.xxs))

                        Row(horizontalArrangement = Arrangement.Start) {
                            Spacer(modifier = Modifier.width(iconSize + iconTextSpacerWidth))
                            Text(
                                stringResource(id = R.string.firstTimeUser_personalPINIntro_info_body),
                                style = UseIdTheme.typography.bodyMRegular,
                                color = UseIdTheme.colors.black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Image(
                    painter = painterResource(id = R.drawable.eid_3),
                    contentScale = ContentScale.Inside,
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

interface SetupPersonalPinIntroViewModelInterface {
    fun onSetPin()
    fun onBackButtonClicked()
}

@HiltViewModel
class SetupPersonalPinIntroViewModel @Inject constructor(private val coordinator: SetupCoordinator) :
    ViewModel(),
    SetupPersonalPinIntroViewModelInterface {
    override fun onSetPin() {
        coordinator.onPersonalPinIntroFinished()
    }

    override fun onBackButtonClicked() = coordinator.onBackClicked()
}

//region Preview
private class PreviewSetupPersonalPinIntroViewModel : SetupPersonalPinIntroViewModelInterface {
    override fun onSetPin() { }
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
