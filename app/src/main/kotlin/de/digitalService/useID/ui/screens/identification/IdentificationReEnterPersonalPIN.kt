package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun IdentificationReEnterPersonalPIN(
    modifier: Modifier = Modifier,
    viewModel: IdentificationReEnterPersonalPINViewModel
) {
    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            stringResource(id = R.string.identification_personalPIN_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        PINEntryField(
            value = viewModel.pin,
            digitCount = 6,
            obfuscation = false,
            spacerPosition = 3,
            onValueChanged = viewModel::userInputPIN,
            contentDescription = pinEntryFieldDescription,
            focusRequester = focusRequester,
            onDone = viewModel::onDoneTapped,
            modifier = Modifier
                .padding(top = 50.dp)
                .width(240.dp)
                .height(56.dp)
        )
        Text(
            stringResource(id = R.string.identification_personalPIN_error_incorrectPIN),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Text(
            stringResource(id = R.string.identification_personalPIN_error_tryAgain),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Text(
            LocalContext.current.resources.getQuantityString(
                R.plurals.identification_personalPIN_remainingAttempts,
                viewModel.attempts,
                viewModel.attempts
            ),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

class IdentificationReEnterPersonalPINViewModel(val attempts: Int, private val onDone: (String) -> Unit) {
    var pin by mutableStateOf("")
        private set

    fun userInputPIN(value: String) {
        if (!checkPINString(value)) return

        pin = value
    }

    fun onDoneTapped() {
        if (pin.length == 6) {
            onDone(pin)
        }
    }

    private fun checkPINString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

@Preview
@Composable
fun PreviewIdentificationReEnterPersonalPIN() {
    UseIDTheme {
        IdentificationReEnterPersonalPIN(viewModel = IdentificationReEnterPersonalPINViewModel(3, { }))
    }
}
