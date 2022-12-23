package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationFetchMetadataNavArgs::class)
@Composable
fun IdentificationFetchMetadata(
    modifier: Modifier = Modifier,
    viewModel: IdentificationFetchMetadataViewModelInterface = hiltViewModel<IdentificationFetchMetadataViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { !viewModel.backAllowed },
            onClick = viewModel::onNavigationButtonClicked,
        )
    ) { topPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(UseIdTheme.spaces.m)
                .padding(top = topPadding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(.5f))
            Box {
                CircularProgressIndicator(
                    color = UseIdTheme.colors.blue800,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(70.dp)
                        .testTag("ProgressIndicator")
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(.5f)
            ) {
                Text(
                    stringResource(id = R.string.identification_fetchMetadata_pleaseWait),
                    style = UseIdTheme.typography.bodyLRegular
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                Text(
                    stringResource(id = R.string.identification_fetchMetadata_loadingData),
                    style = UseIdTheme.typography.bodyMRegular
                )
            }
        }
    }
}

data class IdentificationFetchMetadataNavArgs(
    val backAllowed: Boolean
)

interface IdentificationFetchMetadataViewModelInterface {
    fun onNavigationButtonClicked()
    val backAllowed: Boolean
}

@HiltViewModel
class IdentificationFetchMetadataViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationFetchMetadataViewModelInterface {
    override val backAllowed: Boolean

    init {
        backAllowed = IdentificationFetchMetadataDestination.argsFrom(savedStateHandle).backAllowed
    }

    override fun onNavigationButtonClicked() {
        if (backAllowed) {
            coordinator.onBack()
        } else {
            coordinator.cancelIdentification()
        }
    }
}

class PreviewIdentificationFetchMetadataViewModel() : IdentificationFetchMetadataViewModelInterface {
    override fun onNavigationButtonClicked() {}
    override val backAllowed: Boolean = false
}

@Preview(device = Devices.PIXEL_3A, showBackground = true)
@Composable
fun PreviewIdentificationFetchMetadata() {
    UseIdTheme {
        IdentificationFetchMetadata(
            viewModel = PreviewIdentificationFetchMetadataViewModel()
        )
    }
}
