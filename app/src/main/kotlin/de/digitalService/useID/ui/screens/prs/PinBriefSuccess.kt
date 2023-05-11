package de.digitalService.useID.ui.screens.prs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardButtonScreen
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.navigation.AppNavigator
import de.digitalService.useID.ui.screens.destinations.PinBriefSuccessDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import javax.inject.Inject

@Composable
@Destination(
    navArgsDelegate = PinBriefSuccessNavArgs::class
)
fun PinBriefSuccess(viewModel: PinBriefSuccessViewModelInterface = hiltViewModel<PinBriefSuccessViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Cancel, onClick = viewModel::onBack, confirmation = null)
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(title = "Fertig", viewModel::onFinish),
        ) {
            StandardStaticComposition(
                title = "Ihre Bestellung wurde erfolgreich aufgegeben.",
                body = markDownResource(id = R.string.pinBriefSuccess_body),
                imageId = R.drawable.pigeon,
                imageScaling = ContentScale.FillWidth,
                modifier = Modifier.padding(top = topPadding)
            )
        }

    }
}

data class PinBriefSuccessNavArgs(val redirectUrl: String)

interface PinBriefSuccessViewModelInterface {
    val redirectUrl: String
    fun onBack()
    fun onFinish()
}

@HiltViewModel
class PinBriefSuccessViewModel @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val navigator: AppNavigator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), PinBriefSuccessViewModelInterface {

    override val redirectUrl: String

    init {
        redirectUrl = PinBriefSuccessDestination.argsFrom(savedStateHandle).redirectUrl
    }

    override fun onBack() {
        navigator.pop()
    }

    override fun onFinish() {
        appCoordinator.prsSuccess(redirectUrl)
    }
}

private class PreviewPinBriefSuccessViewModel : PinBriefSuccessViewModelInterface {
    override val redirectUrl: String = ""

    override fun onBack() {}
    override fun onFinish() {}
}

@Composable
@Preview
fun PinBriefSuccessPreview() {
    UseIdTheme {
        PinBriefSuccess(PreviewPinBriefSuccessViewModel())
    }
}
