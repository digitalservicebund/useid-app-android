package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupIntroDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = SetupIntroNavArgs::class)
@Composable
fun SetupIntro(viewModel: SetupIntroViewModelInterface = hiltViewModel<SetupIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { viewModel.confirmCancellation },
            onClick = viewModel::onCancelSetup,
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_intro_title),
            body = stringResource(id = R.string.firstTimeUser_intro_body),
            imageID = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            imageModifier = Modifier.fillMaxWidth(),
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_intro_startSetup),
                action = viewModel::onFirstTimeUsage
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_intro_skipSetup),
                action = viewModel::onNonFirstTimeUsage
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

data class SetupIntroNavArgs(
    val confirmCancellation: Boolean
)

interface SetupIntroViewModelInterface {
    val confirmCancellation: Boolean
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
    fun onCancelSetup()
}

@HiltViewModel
class SetupIntroViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), SetupIntroViewModelInterface {

    override val confirmCancellation: Boolean

    init {
        confirmCancellation = SetupIntroDestination.argsFrom(savedStateHandle).confirmCancellation
    }

    override fun onFirstTimeUsage() {
        setupCoordinator.startSetupIdCard()
    }

    override fun onNonFirstTimeUsage() {
        setupCoordinator.skipSetup()
    }

    override fun onCancelSetup() {
        setupCoordinator.cancelSetup()
    }
}

//region Preview
private class PreviewSetupIntroViewModel : SetupIntroViewModelInterface {
    override val confirmCancellation: Boolean = false
    override fun onFirstTimeUsage() {}
    override fun onNonFirstTimeUsage() {}
    override fun onCancelSetup() {}
}

@Composable
@Preview
fun PreviewSetupIntro() {
    UseIdTheme {
        SetupIntro(PreviewSetupIntroViewModel())
    }
}
//endregion
