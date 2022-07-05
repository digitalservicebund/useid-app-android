package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.screens.BundButtonConfig
import de.digitalService.useID.ui.composables.screens.StandardButtonScreen
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationSuccessDestination
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
    StandardButtonScreen(
        primaryButton = BundButtonConfig(
            stringResource(id = R.string.identification_success_button),
            action = viewModel::onButtonTapped
        )
    ) {
        Column(verticalArrangement = Arrangement.Center, modifier = modifier.fillMaxHeight().padding(horizontal = 20.dp)) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(id = R.string.identification_success_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(20.dp))

            val provider = viewModel.provider
            val bodyString = stringResource(id = R.string.identification_success_body, provider)
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
    fun onButtonTapped()
}

data class IdentificationSuccessNavArgs(
    val provider: String
)

@HiltViewModel
class IdentificationSuccessViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    ViewModel(),
    IdentificationSuccessViewModelInterface {
    override val provider: String
    override fun onButtonTapped() {}

    init {
        provider = IdentificationSuccessDestination.argsFrom(savedStateHandle).provider
    }
}

@Preview
@Composable
fun PreviewIdentificationSuccess() {
    UseIDTheme {
        IdentificationSuccess(
            viewModel = object : IdentificationSuccessViewModelInterface {
                override val provider: String = "Grundsteuer"
                override fun onButtonTapped() {}
            }
        )
    }
}
