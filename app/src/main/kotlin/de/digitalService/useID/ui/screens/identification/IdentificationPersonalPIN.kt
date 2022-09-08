package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PINEntryField
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun IdentificationPersonalPIN(
    modifier: Modifier = Modifier,
    viewModel: IdentificationPersonalPINViewModelInterface = hiltViewModel<IdentificationPersonalPINViewModel>()
) {
    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    val focusRequester = remember { FocusRequester() }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            onClick = viewModel::onCancelButtonTapped)
    ) { topPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(horizontal = 20.dp).padding(top = topPadding)
        ) {
            Text(
                stringResource(id = R.string.identification_personalPIN_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            PINEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequester,
                onDone = viewModel::onDone,
                modifier = Modifier
                    .padding(top = 50.dp)
                    .width(240.dp)
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

interface IdentificationPersonalPINViewModelInterface {
    val pin: String

    fun userInputPIN(value: String)
    fun onDone()
    fun onCancelButtonTapped()
}

@HiltViewModel
class IdentificationPersonalPINViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
) : ViewModel(), IdentificationPersonalPINViewModelInterface {
    override var pin by mutableStateOf("")
        private set

    override fun userInputPIN(value: String) {
        if (!checkPINString(value)) return
        pin = value
    }

    override fun onDone() {
        if (pin.length == 6) {
            coordinator.onPINEntered(pin)
        }
    }

    override fun onCancelButtonTapped() = coordinator.cancelIdentification()

    private fun checkPINString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

class PreviewIdentificationPersonalPINViewModel(
    override val pin: String
) : IdentificationPersonalPINViewModelInterface {
    override fun userInputPIN(value: String) {}
    override fun onDone() {}
    override fun onCancelButtonTapped() {}
}

@Preview
@Composable
fun PreviewIdentificationPersonalPIN() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123"))
    }
}
