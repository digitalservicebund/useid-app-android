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
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.IdCardAttribute
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
            icon = if (viewModel.didSetup) NavigationIcon.Back else NavigationIcon.Cancel,
            shouldShowConfirmDialog = !viewModel.didSetup,
            onClick = viewModel::onCancelButtonClicked,
            isIdentification = true
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
                    viewModel.identificationProvider,
                    style = UseIdTheme.typography.headingXl
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                Text(
                    stringResource(
                        id = R.string.identification_attributeConsent_body,
                        viewModel.identificationProvider
                    ),
                    style = UseIdTheme.typography.bodyLRegular
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                AttributeList(attributeIDs = viewModel.requiredReadAttributes)
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                BundButton(
                    type = ButtonType.SECONDARY,
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
                onClick = onDismissalRequest
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
                Text(content.subject, style = UseIdTheme.typography.headingXl)
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_providerInfo),
                    style = UseIdTheme.typography.headingMBold
                )
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_provider),
                    style = UseIdTheme.typography.bodyLBold
                )
                Column {
                    Text(
                        content.subject,
                        style = UseIdTheme.typography.bodyLRegular
                    )
                    Text(
                        content.subjectURL,
                        style = UseIdTheme.typography.bodyLRegular
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_issuer),
                    style = UseIdTheme.typography.bodyLBold
                )
                Column {
                    Text(
                        content.issuer,
                        style = UseIdTheme.typography.bodyLRegular
                    )
                    Text(
                        content.issuerURL,
                        style = UseIdTheme.typography.bodyLRegular
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsentInfo_providerInfo),
                    style = UseIdTheme.typography.bodyLBold
                )
                Text(
                    content.terms,
                    style = UseIdTheme.typography.bodyLRegular
                )
            }
        }
    }
}

data class IdentificationAttributeConsentNavArgs(
    val request: EidAuthenticationRequest
)

data class ProviderInfoDialogContent(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val terms: String
) {
    constructor(request: EidAuthenticationRequest) : this(
        request.issuer,
        request.issuerURL,
        request.subject,
        request.subjectURL,
        (request.terms as AuthenticationTerms.Text).text
    )
}

interface IdentificationAttributeConsentViewModelInterface {
    val identificationProvider: String
    val requiredReadAttributes: List<Int>

    val shouldShowInfoDialog: Boolean
    val infoDialogContent: ProviderInfoDialogContent

    val didSetup: Boolean

    fun onInfoButtonClicked()
    fun onInfoDialogDismissalRequest()
    fun onPinButtonClicked()
    fun onCancelButtonClicked()
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

    override val didSetup: Boolean
        get() = coordinator.didSetup

    init {
        val request = IdentificationAttributeConsentDestination.argsFrom(savedStateHandle).request
        identificationProvider = request.subject
        requiredReadAttributes = request
            .readAttributes
            .filterValues { it }
            .keys
            .map { attributeDescriptionID(it) }
        infoDialogContent = ProviderInfoDialogContent(request)
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

    override fun onCancelButtonClicked() {
        coordinator.cancelIdentification()
    }

    private fun attributeDescriptionID(attribute: IdCardAttribute): Int = when (attribute) {
        IdCardAttribute.DG01 -> R.string.cardAttribute_dg01
        IdCardAttribute.DG02 -> R.string.cardAttribute_dg02
        IdCardAttribute.DG03 -> R.string.cardAttribute_dg03
        IdCardAttribute.DG04 -> R.string.cardAttribute_dg04
        IdCardAttribute.DG05 -> R.string.cardAttribute_dg05
        IdCardAttribute.DG06 -> R.string.cardAttribute_dg06
        IdCardAttribute.DG07 -> R.string.cardAttribute_dg07
        IdCardAttribute.DG08 -> R.string.cardAttribute_dg08
        IdCardAttribute.DG09 -> R.string.cardAttribute_dg09
        IdCardAttribute.DG10 -> R.string.cardAttribute_dg10
        IdCardAttribute.DG13 -> R.string.cardAttribute_dg13
        IdCardAttribute.DG17 -> R.string.cardAttribute_dg17
        IdCardAttribute.DG19 -> R.string.cardAttribute_dg19
        IdCardAttribute.RESTRICTED_IDENTIFICATION -> R.string.cardAttribute_restrictedIdentification
        IdCardAttribute.AGE_VERIFICATION -> R.string.cardAttribute_ageVerification
    }
}

class PreviewIdentificationAttributeConsentViewModel(
    override val identificationProvider: String,
    override val requiredReadAttributes: List<Int>,
    override val shouldShowInfoDialog: Boolean,
    override val infoDialogContent: ProviderInfoDialogContent
) :
    IdentificationAttributeConsentViewModelInterface {
    override val didSetup: Boolean = false
    override fun onInfoButtonClicked() {}
    override fun onInfoDialogDismissalRequest() {}
    override fun onPinButtonClicked() {}
    override fun onCancelButtonClicked() {}
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
            R.string.cardAttribute_dg19,
            R.string.cardAttribute_restrictedIdentification
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
