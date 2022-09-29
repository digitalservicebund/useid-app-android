package de.digitalService.useID.ui.screens.identification

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.StandardButtonScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationSuccessDestination
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination(
    navArgsDelegate = IdentificationSuccessNavArgs::class
)
@Composable
fun IdentificationSuccess(
    modifier: Modifier = Modifier,
    viewModel: IdentificationSuccessViewModelInterface = hiltViewModel<IdentificationSuccessViewModel>()
) {
    val context = LocalContext.current

    StandardButtonScreen(
        primaryButton = BundButtonConfig(
            stringResource(id = R.string.identification_done_continue),
            action = { viewModel.onButtonTapped(context) }
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .fillMaxHeight()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(stringResource(id = R.string.identification_done_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(20.dp))

            val provider = viewModel.provider
            val bodyString = stringResource(id = R.string.identification_done_body, provider)
            val providerStartIndex = bodyString.indexOf(provider)
            val annotatedString = buildAnnotatedString {
                append(bodyString)
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start = providerStartIndex, end = providerStartIndex + provider.length)
            }
            Text(annotatedString)
        }
    }
}

interface IdentificationSuccessViewModelInterface {
    val provider: String
    fun onButtonTapped(context: Context)
}

data class IdentificationSuccessNavArgs(
    val provider: String,
    val refreshAddress: String
)

@HiltViewModel
class IdentificationSuccessViewModel @Inject constructor(
    private val identificationCoordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationSuccessViewModelInterface {
    override val provider: String
    private val refreshAddress: String

    override fun onButtonTapped(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(refreshAddress))
        startActivity(context, intent, null)

        identificationCoordinator.finishIdentification()
    }

    init {
        provider = IdentificationSuccessDestination.argsFrom(savedStateHandle).provider
        refreshAddress = IdentificationSuccessDestination.argsFrom(savedStateHandle).refreshAddress
    }
}

@Preview
@Composable
fun PreviewIdentificationSuccess() {
    UseIDTheme {
        IdentificationSuccess(
            viewModel = object : IdentificationSuccessViewModelInterface {
                override val provider: String = "Grundsteuer"
                override fun onButtonTapped(context: Context) {}
            }
        )
    }
}
