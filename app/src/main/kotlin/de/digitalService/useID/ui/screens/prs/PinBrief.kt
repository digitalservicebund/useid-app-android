package de.digitalService.useID.ui.screens.prs

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Composable
@Destination
fun PinBrief(viewModel: PinBriefViewModelInterface = hiltViewModel<PinBriefViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBack, confirmation = null)
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(title = "Brief kostenlos bestellen", viewModel::onBriefBestellenClicked),
        ) {
            StandardStaticComposition(
                title = stringResource(R.string.firstTimeUser_missingPINLetter_title),
                body = "Ihr Brief mit Aktivierungscode und neuer PIN kommt direkt zu Ihnen nach Hause.",
                imageId = R.drawable.ic_illustration_pin_letter,
                imageScaling = ContentScale.FillWidth,
                modifier = Modifier.padding(top = topPadding)
            )
        }

    }
}

interface PinBriefViewModelInterface {
    fun onBack()
    fun onBriefBestellenClicked()
}

@HiltViewModel
class PinBriefViewModel @Inject constructor(private val appCoordinator: AppCoordinator, private val navigator: AppNavigator) : ViewModel(), PinBriefViewModelInterface {
    override fun onBack() {
        navigator.pop()
    }

    override fun onBriefBestellenClicked() {
        appCoordinator.prsLink("eid://127.0.0.1:24727/eID-Client?tcTokenURL=https://demo.pinreset.bundesdruckerei.de/bestellung/tc-token")
    }
}

private class PreviewPinBriefViewModel : PinBriefViewModelInterface {
    override fun onBack() {}
    override fun onBriefBestellenClicked() {}
}

@Composable
@Preview
fun PinBriefPreview() {
    UseIdTheme {
        PinBrief(PreviewPinBriefViewModel())
    }
}
