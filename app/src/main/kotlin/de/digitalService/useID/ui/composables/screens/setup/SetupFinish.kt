package de.digitalService.useID.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun SetupFinish(viewModel: SetupFinishViewModelInterface) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Text(
            "success_title",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "success_body",
            style = MaterialTheme.typography.bodySmall
        )
        BundButton(type = ButtonType.PRIMARY, onClick = viewModel::onCloseButtonClicked, label = "Close")
    }
}

interface SetupFinishViewModelInterface {
    fun onCloseButtonClicked()
}

@HiltViewModel
class SetupFinishViewModel @Inject constructor(private val coordinator: SetupCoordinator): ViewModel(), SetupFinishViewModelInterface {
    override fun onCloseButtonClicked() {
        coordinator.onSetupFinished()
    }
}

class PreviewSetupFinishViewModel: SetupFinishViewModelInterface {
    override fun onCloseButtonClicked() { }
}

@Preview
@Composable
fun PreviewSetupFinish() {
    UseIDTheme {
        SetupFinish(PreviewSetupFinishViewModel())
    }
}
