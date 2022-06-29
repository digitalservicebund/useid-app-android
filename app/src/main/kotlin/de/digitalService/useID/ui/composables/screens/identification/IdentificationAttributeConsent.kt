package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.RegularBundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPIN
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPINViewModel
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import org.openecard.bouncycastle.math.raw.Mod
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
    Scaffold(bottomBar = {
        RegularBundButton(
            type = ButtonType.PRIMARY,
            onClick = viewModel::onPINButtonTapped,
            label = stringResource(id = R.string.identification_attributeConsent_pinButton),
            modifier = Modifier
                .padding(20.dp)
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                viewModel.identificationProvider,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.padding(5.dp))
            Text(
                stringResource(
                    id = R.string.identification_attributeConsent_body,
                    viewModel.identificationProvider
                ),
                style = MaterialTheme.typography.bodySmall
            )
            AttributeList(attributeIDs = viewModel.requiredReadAttributes)
            Spacer(Modifier.height(20.dp))
            BundButton(
                type = ButtonType.SECONDARY,
                onClick = viewModel::onInfoButtonTapped,
                label = stringResource(id = R.string.identification_attributeConsent_button_additionalInformation)
            )
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
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.secondary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 40.dp, top = 20.dp)) {
            attributeIDs.forEach { attributeId ->
                Text(
                    "\u2022 ${stringResource(id = attributeId)}",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoDialog(content: ProviderInfoDialogContent, onDismissalRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissalRequest) {
        ScreenWithTopBar(
            navigationIcon = {
                IconButton(onClick = onDismissalRequest) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.navigation_cancel)
                    )
                }
            },
            modifier = Modifier.height(500.dp)
        ) { topPadding ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(top = topPadding, start = 20.dp, end = 20.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                val distance = 10.dp
                Text(content.subject, style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(id = R.string.identification_attributeConsent_info_provider_info),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(id = R.string.identification_attributeConsent_info_provider),
                    style = MaterialTheme.typography.titleSmall
                )
                Column {
                    Text(
                        content.subject,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        content.subjectURL,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsent_info_issuer),
                    style = MaterialTheme.typography.titleSmall
                )
                Column {
                    Text(
                        content.issuer,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        content.issuerURL,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    stringResource(id = R.string.identification_attributeConsent_info_provider_info),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    content.terms,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

data class IdentificationAttributeConsentNavArgs(
    val request: EIDAuthenticationRequest
)

data class ProviderInfoDialogContent(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val terms: String
) {
    constructor(request: EIDAuthenticationRequest) : this(
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

    fun onInfoButtonTapped()
    fun onInfoDialogDismissalRequest()
    fun onPINButtonTapped()
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

    override fun onInfoButtonTapped() {
        shouldShowInfoDialog = true
    }

    override fun onInfoDialogDismissalRequest() {
        shouldShowInfoDialog = false
    }

    override fun onPINButtonTapped() {
        coordinator.confirmAttributesForIdentification()
    }

    private fun attributeDescriptionID(attribute: IDCardAttribute): Int = when (attribute) {
        IDCardAttribute.DG01 -> R.string.idCardAttribute_DG01
        IDCardAttribute.DG02 -> R.string.idCardAttribute_DG02
        IDCardAttribute.DG03 -> R.string.idCardAttribute_DG03
        IDCardAttribute.DG04 -> R.string.idCardAttribute_DG04
        IDCardAttribute.DG05 -> R.string.idCardAttribute_DG05
        IDCardAttribute.DG06 -> R.string.idCardAttribute_DG06
        IDCardAttribute.DG07 -> R.string.idCardAttribute_DG07
        IDCardAttribute.DG08 -> R.string.idCardAttribute_DG08
        IDCardAttribute.DG09 -> R.string.idCardAttribute_DG09
        IDCardAttribute.DG10 -> R.string.idCardAttribute_DG10
        IDCardAttribute.DG13 -> R.string.idCardAttribute_DG13
        IDCardAttribute.DG17 -> R.string.idCardAttribute_DG17
        IDCardAttribute.DG19 -> R.string.idCardAttribute_DG19
        IDCardAttribute.RESTRICTED_IDENTIFICATION -> R.string.idCardAttribute_restrictedIdentification
        IDCardAttribute.AGE_VERIFICATION -> R.string.idCardAttribute_ageVerification
    }
}

class PreviewIdentificationAttributeConsentViewModel(
    override val identificationProvider: String,
    override val requiredReadAttributes: List<Int>,
    override val shouldShowInfoDialog: Boolean,
    override val infoDialogContent: ProviderInfoDialogContent
) :
    IdentificationAttributeConsentViewModelInterface {
    override fun onInfoButtonTapped() {}
    override fun onInfoDialogDismissalRequest() {}
    override fun onPINButtonTapped() {}
}

private fun previewIdentificationAttributeConsentViewModel(infoDialog: Boolean): IdentificationAttributeConsentViewModelInterface =
    PreviewIdentificationAttributeConsentViewModel(
        identificationProvider = "Grundsteuer",
        requiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG03,
            R.string.idCardAttribute_DG04,
            R.string.idCardAttribute_DG05,
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

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationAttributeConsent() {
    UseIDTheme {
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
    UseIDTheme {
        IdentificationAttributeConsent(
            viewModel = previewIdentificationAttributeConsentViewModel(
                true
            )
        )
    }
}
