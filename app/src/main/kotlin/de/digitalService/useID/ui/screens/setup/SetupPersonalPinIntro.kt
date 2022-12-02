package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
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
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        stringResource(id = R.string.firstTimeUser_personalPINIntro_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "",
                                    modifier = Modifier.padding(end = 6.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = stringResource(R.string.firstTimeUser_personalPINIntro_info_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                stringResource(id = R.string.firstTimeUser_personalPINIntro_info_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Image(
                        painter = painterResource(id = R.drawable.eid_3_pin),
                        contentScale = ContentScale.Inside,
                        contentDescription = "",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
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
