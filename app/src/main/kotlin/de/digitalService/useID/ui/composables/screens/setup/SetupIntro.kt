package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.screens.destinations.SetupIntroDestination
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@RootNavGraph(start = true)
@Destination(
    navArgsDelegate = SetupIntroNavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "eid://127.0.0.1:24727/eID-Client?tcTokenURL={tcTokenURL}")
    ]
)
@Composable
fun SetupIntro(viewModel: SetupIntroViewModelInterface = hiltViewModel<SetupIntroViewModel>()) {
    StandardStaticComposition(
        title = stringResource(id = R.string.firstTimeUser_intro_title),
        body = stringResource(id = R.string.firstTimeUser_intro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_no), action = viewModel::onFirstTimeUsage),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_yes), action = viewModel::onNonFirstTimeUsage)
    )
}

data class SetupIntroNavArgs(
    val tcTokenURL: String?
)

interface SetupIntroViewModelInterface {
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
}

@HiltViewModel
class SetupIntroViewModel @Inject constructor(private val setupCoordinator: SetupCoordinator, savedStateHandle: SavedStateHandle) : ViewModel(), SetupIntroViewModelInterface {
    init {
        SetupIntroDestination.argsFrom(savedStateHandle).tcTokenURL?.let {
            setupCoordinator.setTCTokenURL(it)
        }
    }

    override fun onFirstTimeUsage() {
        setupCoordinator.startSetupIDCard()
    }

    override fun onNonFirstTimeUsage() {
        setupCoordinator.onSetupFinished()
    }
}

//region Preview
private class PreviewSetupIntroViewModel() : SetupIntroViewModelInterface {
    override fun onFirstTimeUsage() { }
    override fun onNonFirstTimeUsage() { }
}

@Composable
@Preview
fun PreviewSetupIntro() {
    UseIDTheme {
        SetupIntro(PreviewSetupIntroViewModel())
    }
}
//endregion
