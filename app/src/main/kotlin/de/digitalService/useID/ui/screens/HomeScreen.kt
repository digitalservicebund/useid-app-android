package de.digitalService.useID.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.BuildConfig
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.BundInformationButton
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.AccessibilityScreenDestination
import de.digitalService.useID.ui.screens.destinations.DependenciesScreenDestination
import de.digitalService.useID.ui.screens.destinations.ImprintScreenDestination
import de.digitalService.useID.ui.screens.destinations.PrivacyScreenDestination
import de.digitalService.useID.ui.screens.destinations.TermsOfUseScreenDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.AbTestManager
import javax.inject.Inject

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen(viewModel: HomeScreenViewModelInterface = hiltViewModel<HomeScreenViewModel>()) {
    LaunchedEffect(Unit) {
        viewModel.homeScreenLaunched()
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            UseIdTheme.colors.blue100,
                            UseIdTheme.colors.blue200
                        )
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(top = 52.dp, bottom = 32.dp)
                    .semantics { testTag = R.drawable.img_logo.toString() }
            )

            Text(
                text = stringResource(R.string.home_header_title),
                style = UseIdTheme.typography.bodyLBold,
                color = UseIdTheme.colors.blue800,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

            Text(
                text = stringResource(R.string.home_header_infoText),
                style = UseIdTheme.typography.bodyLRegular,
                color = UseIdTheme.colors.black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

            Text(
                text = stringResource(R.string.home_header_infoCTA),
                style = UseIdTheme.typography.bodyLBold,
                color = UseIdTheme.colors.black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

            Image(
                painter = painterResource(id = R.drawable.abstract_widget_phone),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .semantics { testTag = R.drawable.abstract_widget_phone.toString() }
            )

            Spacer(modifier = Modifier.height(52.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = UseIdTheme.spaces.m)
        ) {
            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            SetupUseIdCardBox(viewModel)

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            SelbstauskunftCardBox(viewModel = viewModel)

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))


            MoreSettingsCardBox(viewModel)

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = UseIdTheme.colors.neutrals900,
                style = UseIdTheme.typography.captionL,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xl))
        }
    }
}

@Composable
private fun CardBox(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = UseIdTheme.shapes.roundedSmall,
        colors = CardDefaults.cardColors(containerColor = UseIdTheme.colors.white),
        border = BorderStroke(1.dp, UseIdTheme.colors.neutrals300),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = UseIdTheme.shapes.roundedSmall,
                spotColor = UseIdTheme.colors.neutrals300
            ),
        content = content
    )
}

@Composable
private fun SetupUseIdCardBox(viewModel: HomeScreenViewModelInterface) {
    CardBox {
        Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

        Text(
            text = stringResource(R.string.home_setup_title),
            style = UseIdTheme.typography.headingMBold,
            color = UseIdTheme.colors.black,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.m)
                .semantics { heading() }
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

        Text(
            text = stringResource(R.string.home_setup_body),
            style = UseIdTheme.typography.bodyMRegular,
            color = UseIdTheme.colors.black,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.m)
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

        BundInformationButton(
            onClick = viewModel::setupOnlineId,
            label = stringResource(if (viewModel.showVariation) R.string.home_setup_setupVariation else R.string.home_setup_setup),
            modifier = Modifier
                .padding(horizontal = UseIdTheme.spaces.m)
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
    }
}

@Composable
private fun SelbstauskunftCardBox(viewModel: HomeScreenViewModelInterface) {
    CardBox {
        Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

        Text(
            text = "Ausweisfunktion testen",
            style = UseIdTheme.typography.headingMBold,
            color = UseIdTheme.colors.black,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.m)
                .semantics { heading() }
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

        Text(
            text = "Überprüfen Sie, ob Ihr Ausweis einsatzbereit ist.",
            style = UseIdTheme.typography.bodyMRegular,
            color = UseIdTheme.colors.black,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.m)
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

        BundInformationButton(
            onClick = viewModel::checkAusweis,
            label = "Ausweisfunktion testen",
            modifier = Modifier
                .padding(horizontal = UseIdTheme.spaces.m)
        )

        Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
    }
}

@Composable
private fun MoreSettingsCardBox(viewModel: HomeScreenViewModelInterface) {
    CardBox {
        CardButton(text = "PIN Rücksetzbrief bestellen", onClick = viewModel::onPinRücksetzbrief)
        StyledDivider()

        CardButton(text = stringResource(R.string.home_more_privacy), onClick = viewModel::onPrivacyButtonClicked)
        StyledDivider()

        CardButton(text = stringResource(R.string.home_more_licenses), onClick = viewModel::onLicenseButtonClicked)
        StyledDivider()

        CardButton(
            text = stringResource(R.string.home_more_accessibilityStatement),
            onClick = viewModel::onAccessibilityButtonClicked
        )
        StyledDivider()

        CardButton(text = stringResource(R.string.home_more_terms), onClick = viewModel::onTermsOfUseButtonClicked)
        StyledDivider()

        CardButton(text = stringResource(R.string.home_more_imprint), onClick = viewModel::onImprintButtonClicked)
    }
}

@Composable
private fun StyledDivider() {
    Divider(
        color = UseIdTheme.colors.neutrals300,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UseIdTheme.spaces.m)
    )
}

@Composable
private fun CardButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        contentPadding = PaddingValues(start = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UseIdTheme.spaces.m)
    ) {
        Text(
            text = text,
            color = UseIdTheme.colors.black,
            style = UseIdTheme.typography.bodyMRegular,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

interface HomeScreenViewModelInterface {
    val showVariation: Boolean
    fun setupOnlineId()
    fun checkAusweis()
    fun homeScreenLaunched()
    fun onPinRücksetzbrief()
    fun onPrivacyButtonClicked()
    fun onImprintButtonClicked()
    fun onAccessibilityButtonClicked()
    fun onTermsOfUseButtonClicked()
    fun onLicenseButtonClicked()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val appNavigator: Navigator,
    private val trackerManager: TrackerManagerType,
    abTestManager: AbTestManager
) : ViewModel(), HomeScreenViewModelInterface {
    private val logger by getLogger()

    override val showVariation: Boolean by abTestManager.isSetupIntroTestVariation
    override fun homeScreenLaunched() {
        logger.debug("Home screen launched.")
        appCoordinator.homeScreenLaunched()
    }

    override fun setupOnlineId() {
        trackerManager.trackButtonPressed(category = "firstTimeUser", "start")
        appCoordinator.offerIdSetup(null)
    }

    override fun checkAusweis() {
        appCoordinator.startCheck()
    }

    override fun onPinRücksetzbrief() {
        appCoordinator.prs()
    }

    override fun onPrivacyButtonClicked() {
        appNavigator.navigate(PrivacyScreenDestination)
    }

    override fun onAccessibilityButtonClicked() {
        appNavigator.navigate(AccessibilityScreenDestination)
    }

    override fun onTermsOfUseButtonClicked() {
        appNavigator.navigate(TermsOfUseScreenDestination)
    }

    override fun onLicenseButtonClicked() {
        appNavigator.navigate(DependenciesScreenDestination)
    }

    override fun onImprintButtonClicked() {
        appNavigator.navigate(ImprintScreenDestination)
    }
}

private class PreviewViewModel : HomeScreenViewModelInterface {
    override val showVariation = true
    override fun setupOnlineId() {}
    override fun checkAusweis() {}
    override fun homeScreenLaunched() {}
    override fun onPinRücksetzbrief() {}
    override fun onPrivacyButtonClicked() {}
    override fun onImprintButtonClicked() {}
    override fun onAccessibilityButtonClicked() {}
    override fun onTermsOfUseButtonClicked() {}
    override fun onLicenseButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        HomeScreen(PreviewViewModel())
    }
}
