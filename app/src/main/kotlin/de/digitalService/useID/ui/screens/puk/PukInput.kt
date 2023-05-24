package de.digitalService.useID.ui.screens.puk

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.InputType
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.screens.destinations.PukInputDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = PukInputNavArgs::class)
@Composable
fun PukInput(
    viewModel: PukInputViewModelInterface = hiltViewModel<PukInputViewModel>()
) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.puk_input_title),
        body = stringResource(id = R.string.puk_input_body),
        errorMessage = stringResource(id = R.string.puk_input_retryError).takeIf { viewModel.retry },
        entryFieldDescription = stringResource(id = R.string.puk_input_label),
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = null
        ),
        attempts = null,
        inputType = InputType.Puk,
        onDone = viewModel::onDone
    ) {
        Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

        TextButton(onClick = viewModel::onPukNotAvailableButtonClicked) {
            Text(
                stringResource(id = R.string.puk_input_pukUnavailable),
                style = UseIdTheme.typography.bodyLBold,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                color = UseIdTheme.colors.blue800
            )
        }
    }
}

data class PukInputNavArgs(
    val retry: Boolean
)

interface PukInputViewModelInterface {
    val retry: Boolean

    fun onDone(puk: String)
    fun onNavigationButtonClicked()
    fun onPukNotAvailableButtonClicked()
}

@HiltViewModel
class PukInputViewModel @Inject constructor(
    private val pukCoordinator: PukCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), PukInputViewModelInterface {

    override val retry: Boolean

    init {
        retry = PukInputDestination.argsFrom(savedStateHandle).retry
    }

    override fun onDone(puk: String) {
        pukCoordinator.setPuk(puk)
    }

    override fun onNavigationButtonClicked() {
        pukCoordinator.onBack()
    }

    override fun onPukNotAvailableButtonClicked() {

    }
}

class PreviewPukInputViewModel(
    override val retry: Boolean
) : PukInputViewModelInterface {
    override fun onDone(puk: String) {}
    override fun onNavigationButtonClicked() {}
    override fun onPukNotAvailableButtonClicked() {}
}

@Preview
@Composable
fun PreviewPukInput() {
    UseIdTheme {
        PukInput(viewModel = PreviewPukInputViewModel(false))
    }
}

@Preview
@Composable
fun PreviewPukInputRetry() {
    UseIdTheme {
        PukInput(viewModel = PreviewPukInputViewModel(true))
    }
}
