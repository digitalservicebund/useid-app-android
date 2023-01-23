package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationCanIntroDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationCanIntroNavArgs::class)
@Composable
fun IdentificationCanIntro(viewModel: IdentificationCanIntroViewModelInterface = hiltViewModel<IdentificationCanIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { !viewModel.backAllowed },
            onClick = viewModel::onNavigationButtonClicked
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.identification_can_intro_title),
            body = stringResource(id = R.string.identification_can_intro_body),
            imageId = R.drawable.illustration_id_can,
            imageScaling = ContentScale.Inside,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.identification_can_intro_continue),
                action = viewModel::onContinue
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

data class IdentificationCanIntroNavArgs(
    val backAllowed: Boolean
)

interface IdentificationCanIntroViewModelInterface {
    val backAllowed: Boolean

    fun onNavigationButtonClicked()
    fun onContinue()
}

@HiltViewModel
class IdentificationCanIntroViewModel @Inject constructor(
    private val coordinator: CanCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationCanIntroViewModelInterface {
    override val backAllowed: Boolean

    init {
        backAllowed = IdentificationCanIntroDestination.argsFrom(savedStateHandle).backAllowed
    }

    override fun onNavigationButtonClicked() {
        if (backAllowed) {
            coordinator.onBack()
        } else {
            coordinator.cancelCanFlow()
        }
    }

    override fun onContinue() {
        coordinator.finishIntro()
    }
}

private class PreviewIdentificationCanIntroViewModel : IdentificationCanIntroViewModelInterface {
    override val backAllowed: Boolean = false
    override fun onNavigationButtonClicked() {}
    override fun onContinue() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanIntro(PreviewIdentificationCanIntroViewModel())
    }
}
