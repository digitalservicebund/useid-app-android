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
import androidx.compose.ui.Modifier
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
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.ui.theme.Yellow300
import javax.inject.Inject

@Destination
@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface = hiltViewModel<SetupFinishViewModel>()) {
    val finishedButton = BundButtonConfig(
        title = stringResource(id = R.string.firstTimeUser_finish_button),
        action = viewModel::onCloseButtonClicked
    )

    val identifyButton = BundButtonConfig(
        title = stringResource(id = R.string.firstTimeUser_identify_button),
        action = viewModel::onIdentifyButtonClicked
    )

    StandardButtonScreen(
        primaryButton = if (viewModel.hasTcTokenUrl()) null else finishedButton,
        secondaryButton = if (viewModel.hasTcTokenUrl()) finishedButton else identifyButton
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    stringResource(id = R.string.firstTimeUser_finish_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Yellow300),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "",
                                modifier = Modifier.padding(end = 6.dp)
                            )

                            Text(
                                text = stringResource(R.string.firstTimeUser_finish_infoBox_title),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            stringResource(id = R.string.firstTimeUser_finish_infoBox_body),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(painter = painterResource(id = R.drawable.eids), contentDescription = "")
        }
    }
}

interface SetupFinishViewModelInterface {
    fun onCloseButtonClicked()
    fun hasTcTokenUrl(): Boolean
    fun onIdentifyButtonClicked()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupFinishViewModelInterface {
    override fun hasTcTokenUrl(): Boolean {
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
    override fun hasTcTokenUrl(): Boolean = hasTcTokenUrl
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
