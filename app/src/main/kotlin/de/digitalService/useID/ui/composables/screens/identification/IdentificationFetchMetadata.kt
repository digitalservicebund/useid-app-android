package de.digitalService.useID.ui.composables.screens.identification

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.R
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Composable
fun IdentificationFetchMetadata(viewModel: IdentificationFetchMetadataViewModelInterface, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(20.dp).fillMaxSize()) {
        Spacer(modifier = Modifier.weight(.5f))
        Box {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 10.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(70.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(.5f)
        ) {
            Text(
                stringResource(id = R.string.identification_fetchMetadata_pleaseWait),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                stringResource(id = R.string.identification_fetchMetadata_loadingData),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMetadata()
    }
}

interface IdentificationFetchMetadataViewModelInterface {
    fun fetchMetadata()
}

@HiltViewModel
class IdentificationFetchMetadataViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
) : ViewModel(), IdentificationFetchMetadataViewModelInterface {
    override fun fetchMetadata() {
        coordinator.startIdentificationProcess()
    }
}

class PreviewIdentificationFetchMetadataViewModel: IdentificationFetchMetadataViewModelInterface {
    override fun fetchMetadata() { }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationFetchMetadata() {
    UseIDTheme {
        IdentificationFetchMetadata(PreviewIdentificationFetchMetadataViewModel())
    }
}
