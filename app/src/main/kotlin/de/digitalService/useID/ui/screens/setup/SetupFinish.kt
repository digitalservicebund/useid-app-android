package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import javax.inject.Inject

@Destination
@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface = hiltViewModel<SetupFinishViewModel>()) {
    StandardButtonScreen(primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_finish_button), action = viewModel::onCloseButtonClicked)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp, horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(id = R.string.firstTimeUser_finish_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(id = R.string.firstTimeUser_finish_body),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

interface SetupFinishViewModelInterface {
    fun onCloseButtonClicked()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(private val coordinator: SetupCoordinator) : ViewModel(), SetupFinishViewModelInterface {
    override fun onCloseButtonClicked() {
        coordinator.onSetupFinished()
    }
}

class PreviewSetupFinishViewModel : SetupFinishViewModelInterface {
    override fun onCloseButtonClicked() { }
}

@Preview
@Composable
fun PreviewSetupFinish() {
    UseIDTheme {
        SetupFinish(PreviewSetupFinishViewModel())
    }
}
