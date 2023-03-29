package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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
import de.digitalService.useID.util.AbTestManager
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText
import javax.inject.Inject

@Destination(navArgsDelegate = SetupIntroNavArgs::class)
@Composable
fun SetupIntro(viewModel: SetupIntroViewModelInterface = hiltViewModel<SetupIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { viewModel.confirmCancellation },
            onClick = viewModel::onCancelSetup
        )
    ) { topPadding ->
        if (viewModel.showVariation) {
            StandardButtonScreen(
                primaryButton = BundButtonConfig(
                    title = stringResource(id = R.string.firstTimeUser_intro_startSetup),
                    action = viewModel::onFirstTimeUsage
                ),
                secondaryButton = BundButtonConfig(
                    title = stringResource(id = R.string.firstTimeUser_intro_skipSetup),
                    action = viewModel::onNonFirstTimeUsage
                ),
                modifier = Modifier.padding(top = topPadding)
            ) { bottomPadding ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = UseIdTheme.spaces.m)
                        .padding(bottom = bottomPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(id = R.string.firstTimeUser_intro_titleVariation),
                        style = UseIdTheme.typography.headingXl,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = UseIdTheme.colors.blue200),
                        shape = UseIdTheme.shapes.roundedMedium
                    ) {
                        Column(modifier = Modifier.padding(UseIdTheme.spaces.m)) {
                            MarkdownText(
                                markdown = markDownResource(id = R.string.firstTimeUser_intro_box),
                                fontResource = R.font.bundes_sans_dtp_regular,
                                fontSize = UseIdTheme.typography.bodyLRegular.fontSize,
                                color = UseIdTheme.colors.black
                            )

                            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

                            Image(
                                imageVector = ImageVector.vectorResource(id = R.drawable.img_pin_setup),
                                contentDescription = null,
                                modifier = Modifier.semantics { testTag = R.drawable.img_pin_setup.toString() }
                            )
                        }
                    }
                }
            }
        } else {
            StandardStaticComposition(
                title = stringResource(id = R.string.firstTimeUser_intro_title),
                body = stringResource(id = R.string.firstTimeUser_intro_body),
                imageId = R.drawable.eid_3,
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
}

data class SetupIntroNavArgs(
    val confirmCancellation: Boolean
)

interface SetupIntroViewModelInterface {
    val confirmCancellation: Boolean
    val showVariation: Boolean
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
    fun onCancelSetup()
}

@HiltViewModel
class SetupIntroViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    abTestManager: AbTestManager,
    savedStateHandle: SavedStateHandle
) : ViewModel(), SetupIntroViewModelInterface {

    override val confirmCancellation: Boolean
    override val showVariation: Boolean by abTestManager.isSetupIntroTestVariation

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
    override val showVariation = true
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
