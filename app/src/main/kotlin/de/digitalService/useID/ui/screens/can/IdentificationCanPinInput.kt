package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PersonalPinEntryField
import de.digitalService.useID.ui.components.pin.StandardPinScreen
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination
@Composable
fun IdentificationCanPinInput(viewModel: IdentificationCanPinInputViewModelInterface = hiltViewModel<IdentificationCanPinInputViewModel>()) {
    val resources = LocalContext.current.resources

    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            shouldShowConfirmDialog = false,
            isIdentification = true
        )
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(id = R.string.identification_personalPIN_title),
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.weight(1f))

            PersonalPinEntryField(
                value = viewModel.pin,
                onValueChanged = viewModel::userInputPin,
                obfuscation = true,
                onDone = viewModel::onDone,
                focusRequester = focusRequester,
                entryDescription = pinEntryFieldDescription,
                modifier = Modifier.padding(top = 50.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
            val attemptString = resources.getQuantityString(
                R.plurals.identification_personalPIN_remainingAttempts,
                1,
                1
            )
            Text(
                attemptString,
                style = UseIdTheme.typography.bodyLRegular,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

interface IdentificationCanPinInputViewModelInterface {
    val pin: String

    fun onInitialize()
    fun userInputPin(value: String)
    fun onBack()
    fun onDone()
}

@HiltViewModel
class IdentificationCanPinInputViewModel @Inject constructor(
    val coordinator: CanCoordinator
): ViewModel(), IdentificationCanPinInputViewModelInterface {
    override var pin by mutableStateOf("")
        private set

    override fun onInitialize() {
        pin = ""
    }

    override fun userInputPin(value: String) {
        if (!checkPinString(value)) return
        pin = value
    }

    override fun onBack() {
        coordinator.onBack()
    }

    override fun onDone() {
        if (pin.length == 6) {
            coordinator.onPinEntered(pin)
        }
    }

    private fun checkPinString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

private class PreviewIdentificationCanPinInputViewModel: IdentificationCanPinInputViewModelInterface {
    override val pin: String = "123"
    override fun onInitialize() {}
    override fun userInputPin(value: String) {}
    override fun onBack() {}
    override fun onDone() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanPinInput(PreviewIdentificationCanPinInputViewModel())
    }
}
