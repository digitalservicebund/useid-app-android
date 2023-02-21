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
import de.digitalService.useID.ui.screens.destinations.SetupCanIntroDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = SetupCanIntroNavArgs::class)
@Composable
fun SetupCanIntro(viewModel: SetupCanIntroViewModelInterface = hiltViewModel<SetupCanIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            confirmation = (if (viewModel.identificationPending) Flow.Identification else Flow.Setup).takeIf { !viewModel.backAllowed },
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

data class SetupCanIntroNavArgs(
    val backAllowed: Boolean,
    val identificationPending: Boolean
)

interface SetupCanIntroViewModelInterface {
    val backAllowed: Boolean
    val identificationPending: Boolean

    fun onNavigationButtonClicked()
    fun onContinue()
}

@HiltViewModel
class SetupCanIntroViewModel @Inject constructor(
    private val canCoordinator: CanCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), SetupCanIntroViewModelInterface {
    override val backAllowed: Boolean
    override val identificationPending: Boolean

    init {
        val args = SetupCanIntroDestination.argsFrom(savedStateHandle)
        backAllowed = args.backAllowed
        identificationPending = args.identificationPending
    }

    override fun onNavigationButtonClicked() {
        if (backAllowed) {
            canCoordinator.onBack()
        } else {
            canCoordinator.cancelCanFlow()
        }
    }

    override fun onContinue() {
        canCoordinator.finishIntro()
    }
}

private class PreviewSetupCanIntroViewModel : SetupCanIntroViewModelInterface {
    override val backAllowed: Boolean = false
    override val identificationPending: Boolean = false
    override fun onNavigationButtonClicked() {}
    override fun onContinue() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        SetupCanIntro(PreviewSetupCanIntroViewModel())
    }
}
