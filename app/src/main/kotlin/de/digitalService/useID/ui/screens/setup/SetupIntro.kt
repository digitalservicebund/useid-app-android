package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupIntroDestination
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination(
    navArgsDelegate = SetupIntroNavArgs::class,
)
@Composable
fun SetupIntro(viewModel: SetupIntroViewModelInterface = hiltViewModel<SetupIntroViewModel>()) {
    ScreenWithTopBar { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_intro_title),
            body = stringResource(id = R.string.firstTimeUser_intro_body),
            imageID = R.drawable.eid_3,
            imageScaling = ContentScale.Inside,
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
    val tcTokenURL: String?
)

interface SetupIntroViewModelInterface {
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
}

@HiltViewModel
class SetupIntroViewModel @Inject constructor(private val setupCoordinator: SetupCoordinator, savedStateHandle: SavedStateHandle) :
    ViewModel(), SetupIntroViewModelInterface {
    init {
        SetupIntroDestination.argsFrom(savedStateHandle).tcTokenURL?.let {
            setupCoordinator.setTCTokenURL(it)
        }
    }

    override fun onFirstTimeUsage() {
        setupCoordinator.startSetupIDCard()
    }

    override fun onNonFirstTimeUsage() {
        setupCoordinator.onSkipSetup()
    }
}

//region Preview
private class PreviewSetupIntroViewModel() : SetupIntroViewModelInterface {
    override fun onFirstTimeUsage() {}
    override fun onNonFirstTimeUsage() {}
}

@Composable
@Preview
fun PreviewSetupIntro() {
    UseIDTheme {
        SetupIntro(PreviewSetupIntroViewModel())
    }
}
//endregion
