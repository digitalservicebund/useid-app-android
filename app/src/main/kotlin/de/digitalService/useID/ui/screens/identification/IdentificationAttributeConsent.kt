package de.digitalService.useID.ui.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Destination(
    navArgsDelegate = IdentificationAttributeConsentNavArgs::class
)
@Composable
fun IdentificationAttributeConsent(
    modifier: Modifier = Modifier,
    viewModel: IdentificationAttributeConsentViewModelInterface = hiltViewModel<IdentificationAttributeConsentViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.backAllowed) NavigationIcon.Back else NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { !viewModel.backAllowed },
            onClick = viewModel::onNavigationButtonClicked
        )
    ) { topPadding ->
        Scaffold(
            bottomBar = {
                BundButton(
                    type = ButtonType.PRIMARY,
                    onClick = viewModel::onPinButtonClicked,
                    label = stringResource(id = R.string.identification_attributeConsent_continue),
                    modifier = Modifier
                        .padding(UseIdTheme.spaces.m)
                )
            },
            modifier = modifier.padding(top = topPadding),
            containerColor = UseIdTheme.colors.white
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(
                        id = R.string.identification_attributeConsent_title,
                        viewModel.identificationProvider
                    ),
                    style = UseIdTheme.typography.headingXl,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                Text(
                    stringResource(id = R.string.identification_attributeConsent_body),
                    style = UseIdTheme.typography.bodyLRegular
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                AttributeList(attributeIDs = viewModel.requiredReadAttributes)
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                BundInformationButton(
                    onClick = viewModel::onInfoButtonClicked,
                    label = stringResource(id = R.string.identification_attributeConsent_button_additionalInformation)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = viewModel.shouldShowInfoDialog,
        enter = scaleIn(tween(200)),
        exit = scaleOut(tween(100))
    ) {
        InfoDialog(content = viewModel.infoDialogContent, onDismissalRequest = viewModel::onInfoDialogDismissalRequest)
    }
}

@Composable
private fun AttributeList(attributeIDs: List<Int>) {
    Surface(
        shape = UseIdTheme.shapes.roundedMedium,
        shadowElevation = 10.dp,
        color = UseIdTheme.colors.blue100,
        border = BorderStroke(1.dp, UseIdTheme.colors.blue400),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(top = UseIdTheme.spaces.m)
        ) {
            attributeIDs.forEach { attributeId ->
                Text(
                    "\u2022 ${stringResource(id = attributeId)}",
                    color = UseIdTheme.colors.black,
                    style = UseIdTheme.typography.bodyLRegular,
                    modifier = Modifier.padding(bottom = UseIdTheme.spaces.s)
                )
            }
        }
    }
}

@Composable
private fun InfoDialog(content: ProviderInfoDialogContent, onDismissalRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissalRequest) {
        ScreenWithTopBar(
            navigationButton = NavigationButton(
                icon = NavigationIcon.Cancel,
                onClick = onDismissalRequest,
                confirmation = null,
                testTag = "infoDialogCancel"
            ),
            modifier = Modifier.height(500.dp)
        ) { topPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(top = topPadding)
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                Text(
                    content.subject,
                    style = UseIdTheme.typography.headingXl,
                    modifier = Modifier
                        .semantics {
                            testTag = "subjectTitle"
                            heading()
                        }
                )
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_providerInfo),
                    style = UseIdTheme.typography.headingMBold,
                    modifier = Modifier.semantics {
                        testTag = "providerInfoTitle"
                        heading()
                    }
                )
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_provider),
                    style = UseIdTheme.typography.bodyLBold,
                    modifier = Modifier.semantics { heading() }
                )
                Column {
                    Text(
                        content.subject,
                        style = UseIdTheme.typography.bodyLRegular,
                        modifier = Modifier.semantics { testTag = "subjectName" }
                    )
                    Text(
                        content.subjectURL,
                        style = UseIdTheme.typography.bodyLRegular,
                        modifier = Modifier.semantics { testTag = "subjectURL" }
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_issuer),
                    style = UseIdTheme.typography.bodyLBold,
                    modifier = Modifier.semantics { heading() }
                )
                Column {
                    Text(
                        content.issuer,
                        style = UseIdTheme.typography.bodyLRegular,
                        modifier = Modifier.semantics { testTag = "issuerName" }
                    )
                    Text(
                        content.issuerURL,
                        style = UseIdTheme.typography.bodyLRegular,
                        modifier = Modifier.semantics { testTag = "issuerURL" }
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_providerInfo),
                    style = UseIdTheme.typography.bodyLBold,
                    modifier = Modifier.semantics {
                        testTag = "providerInfoSubtitle"
                        heading()
                    }
                )
                Text(
                    content.terms,
                    style = UseIdTheme.typography.bodyLRegular,
                    modifier = Modifier.semantics { testTag = "terms" }
                )
            }
        }
    }
}

data class IdentificationAttributeConsentNavArgs(
    val identificationAttributes: IdentificationAttributes,
    val backAllowed: Boolean
)

data class ProviderInfoDialogContent(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val terms: String
) {
    constructor(request: CertificateDescription) : this(
        request.issuerName,
        request.issuerUrl.toString(),
        request.subjectName,
        request.subjectUrl.toString(),
        request.termsOfUsage
    )
}

interface IdentificationAttributeConsentViewModelInterface {
    val identificationProvider: String
    val requiredReadAttributes: List<Int>

    val shouldShowInfoDialog: Boolean
    val infoDialogContent: ProviderInfoDialogContent

    val backAllowed: Boolean

    fun onInfoButtonClicked()
    fun onInfoDialogDismissalRequest()
    fun onPinButtonClicked()
    fun onNavigationButtonClicked()
}

@HiltViewModel
class IdentificationAttributeConsentViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationAttributeConsentViewModelInterface {
    override val identificationProvider: String
    override val requiredReadAttributes: List<Int>
    override val infoDialogContent: ProviderInfoDialogContent

    override var shouldShowInfoDialog: Boolean by mutableStateOf(false)
        private set

    override val backAllowed: Boolean

    init {
        val args = IdentificationAttributeConsentDestination.argsFrom(savedStateHandle)

        backAllowed = args.backAllowed

        val identificationAttributes = args.identificationAttributes
        identificationProvider = identificationAttributes.certificateDescription.subjectName
        requiredReadAttributes = identificationAttributes.requiredAttributes
            .map { attributeDescriptionID(it) }
        infoDialogContent = ProviderInfoDialogContent(identificationAttributes.certificateDescription)
    }

    override fun onInfoButtonClicked() {
        shouldShowInfoDialog = true
    }

    override fun onInfoDialogDismissalRequest() {
        shouldShowInfoDialog = false
    }

    override fun onPinButtonClicked() {
        coordinator.confirmAttributesForIdentification()
    }

    override fun onNavigationButtonClicked() {
        if (backAllowed) {
            coordinator.onBack()
        } else {
            coordinator.cancelIdentification()
        }
    }

    private fun attributeDescriptionID(eidAttribute: EidAttribute): Int = when (eidAttribute) {
        EidAttribute.DG01 -> R.string.cardAttribute_dg01
        EidAttribute.DG02 -> R.string.cardAttribute_dg02
        EidAttribute.DG03 -> R.string.cardAttribute_dg03
        EidAttribute.DG04 -> R.string.cardAttribute_dg04
        EidAttribute.DG05 -> R.string.cardAttribute_dg05
        EidAttribute.DG06 -> R.string.cardAttribute_dg06
        EidAttribute.DG07 -> R.string.cardAttribute_dg07
        EidAttribute.DG08 -> R.string.cardAttribute_dg08
        EidAttribute.DG09 -> R.string.cardAttribute_dg09
        EidAttribute.DG10 -> R.string.cardAttribute_dg10
        EidAttribute.DG13 -> R.string.cardAttribute_dg13
        EidAttribute.DG17 -> R.string.cardAttribute_dg17
        EidAttribute.DG18 -> R.string.cardAttribute_dg18
        EidAttribute.DG19 -> R.string.cardAttribute_dg19
        EidAttribute.DG20 -> R.string.cardAttribute_dg20
        EidAttribute.PSEUDONYM -> R.string.cardAttribute_pseudonym
        EidAttribute.AGE_VERIFICATION -> R.string.cardAttribute_ageVerification
        EidAttribute.ADDRESS_VERIFICATION -> R.string.cardAttribute_addressVerification
        EidAttribute.WRITE_DG17 -> R.string.cardAttribute_write_dg17
        EidAttribute.WRITE_DG18 -> R.string.cardAttribute_write_dg18
        EidAttribute.WRITE_DG19 -> R.string.cardAttribute_write_dg19
        EidAttribute.WRITE_DG20 -> R.string.cardAttribute_write_dg20
        EidAttribute.CAN_ALLOWED -> R.string.cardAttribute_canAllowed
        EidAttribute.PIN_MANAGEMENT -> R.string.cardAttribute_pinManagement
    }
}

class PreviewIdentificationAttributeConsentViewModel(
    override val identificationProvider: String,
    override val requiredReadAttributes: List<Int>,
    override val shouldShowInfoDialog: Boolean,
    override val infoDialogContent: ProviderInfoDialogContent
) :
    IdentificationAttributeConsentViewModelInterface {
    override val backAllowed: Boolean = false
    override fun onInfoButtonClicked() {}
    override fun onInfoDialogDismissalRequest() {}
    override fun onPinButtonClicked() {}
    override fun onNavigationButtonClicked() {}
}

private fun previewIdentificationAttributeConsentViewModel(infoDialog: Boolean): IdentificationAttributeConsentViewModelInterface =
    PreviewIdentificationAttributeConsentViewModel(
        identificationProvider = "Grundsteuer",
        requiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg03,
            R.string.cardAttribute_dg04,
            R.string.cardAttribute_dg05,
            R.string.cardAttribute_dg06,
            R.string.cardAttribute_dg07,
            R.string.cardAttribute_dg08,
            R.string.cardAttribute_dg09,
            R.string.cardAttribute_dg10,
            R.string.cardAttribute_dg13,
            R.string.cardAttribute_dg17,
            R.string.cardAttribute_dg19
        ),
        shouldShowInfoDialog = infoDialog,
        infoDialogContent = ProviderInfoDialogContent(
            "Issuer",
            "IssuerURL",
            "Subject",
            "SubjectURL",
            "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        )
    )

@Preview(device = Devices.PIXEL_3A, locale = "de")
@Composable
fun PreviewIdentificationAttributeConsent() {
    UseIdTheme {
        IdentificationAttributeConsent(
            viewModel = previewIdentificationAttributeConsentViewModel(
                false
            )
        )
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationAttributeConsentInfoDialog() {
    UseIdTheme {
        IdentificationAttributeConsent(
            viewModel = previewIdentificationAttributeConsentViewModel(
                true
            )
        )
    }
}
