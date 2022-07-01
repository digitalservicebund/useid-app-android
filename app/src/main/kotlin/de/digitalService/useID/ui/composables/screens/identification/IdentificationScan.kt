package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.coordinators.ScanEvent
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@Destination
@Composable
fun IdentificationScan(modifier: Modifier = Modifier, viewModel: IdentificationScanViewModelInterface = hiltViewModel<IdentificationScanViewModel>()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 40.dp, horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.eids),
                contentScale = ContentScale.Fit,
                contentDescription = ""
            )
            Text(
                stringResource(id = R.string.identification_scan_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(id = R.string.identification_scan_body),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = viewModel::onHelpButtonTapped,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(40.dp)
            ) {
                Text(stringResource(id = R.string.firstTimeUser_scan_helpButton))
            }
        }

    AnimatedVisibility(visible = viewModel.shouldShowProgress) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

interface IdentificationScanViewModelInterface {
    val shouldShowProgress: Boolean

    fun onHelpButtonTapped()
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(private val coordinator: IdentificationCoordinator): ViewModel(), IdentificationScanViewModelInterface {
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    init {
        collectScanEvents()
    }

    override fun onHelpButtonTapped() {
    }

    private fun collectScanEvents() {
        viewModelScope.launch {
            coordinator.scanEventFlow.collect { event ->
                when(event) {
                    ScanEvent.CardRequested -> shouldShowProgress = false
                    ScanEvent.CardAttached -> shouldShowProgress = true
                    ScanEvent.Finished -> shouldShowProgress = false
                }
            }
        }
    }
}

private class PreviewIdentificationScanViewModel(override val shouldShowProgress: Boolean): IdentificationScanViewModelInterface {
    override fun onHelpButtonTapped() {}
}

@Preview
@Composable
fun PreviewIdentificationScan() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false))
    }
}

@Preview
@Composable
fun PreviewIdentificationScanWithProgress() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true))
    }
}
