package de.digitalService.useID.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
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
import de.digitalService.useID.ui.components.BundButton
import de.digitalService.useID.ui.components.ButtonType
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.theme.*
import javax.inject.Inject

@OptIn(ExperimentalComposeUiApi::class)
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
                .background(UseIdTheme.colors.blue200)
        ) {
            Image(
                painter = painterResource(id = R.drawable.abstract_widget_phone),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .padding(start = 50.dp, end = 50.dp, top = 60.dp, bottom = 16.dp)
                    .semantics { testTag = R.drawable.abstract_widget_phone.toString() }
            )

            Text(
                text = stringResource(R.string.home_header_title),
                style = UseIdTheme.typography.headingXl,
                color = UseIdTheme.colors.black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

            Text(
                text = stringResource(R.string.home_header_body),
                style = UseIdTheme.typography.bodyLRegular,
                color = UseIdTheme.colors.black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UseIdTheme.spaces.m)
            )

            Spacer(modifier = Modifier.height(50.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = UseIdTheme.spaces.m)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.home_more_title),
                color = UseIdTheme.colors.black,
                style = UseIdTheme.typography.headingXl,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            SetupUseIdCardBox(viewModel)

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
fun SetupIdBoxLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val idCardImage = measurables[0]
        val setupButton = measurables[1]

        val imagePlaceable = idCardImage.measure(constraints)
        val buttonPlaceable = setupButton.measure(constraints)

        val overlapFactor = 0.9

        layout(
            width = constraints.maxWidth,
            height = (imagePlaceable.height * overlapFactor).toInt() + buttonPlaceable.height
        ) {
            imagePlaceable.placeRelative(0, 0)
            buttonPlaceable.placeRelative(
                x = 0,
                y = (imagePlaceable.height * overlapFactor).toInt()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SetupUseIdCardBox(viewModel: HomeScreenViewModelInterface) {
    CardBox {
        SetupIdBoxLayout(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.eid_3),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = UseIdTheme.spaces.s)
                    .padding(horizontal = UseIdTheme.spaces.l)
                    .fillMaxWidth()
                    .semantics { testTag = R.drawable.eid_3.toString() }
            )

            BundButton(
                type = ButtonType.PRIMARY,
                onClick = viewModel::setupOnlineId,
                label = stringResource(R.string.home_startSetup),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = UseIdTheme.spaces.s)
            )
        }
    }
}

@Composable
private fun MoreSettingsCardBox(viewModel: HomeScreenViewModelInterface) {
    CardBox {
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
    fun setupOnlineId()
    fun homeScreenLaunched()
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
    private val trackerManager: TrackerManagerType
) : ViewModel(), HomeScreenViewModelInterface {
    private val logger by getLogger()

    override fun homeScreenLaunched() {
        logger.debug("Home screen launched.")
        appCoordinator.homeScreenLaunched()
    }

    override fun setupOnlineId() {
        appCoordinator.offerIdSetup(null)
        trackerManager.trackEvent(category = "firstTimeUser", action = "buttonPressed", name = "start")
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
    override fun setupOnlineId() {}
    override fun homeScreenLaunched() {}
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
