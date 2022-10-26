package de.digitalService.useID.ui.screens.error

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationCardUnreadableDestination
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationCardUnreadableNavArgs::class)
@Composable
fun IdentificationCardUnreadable(
    viewModel: IdentificationCardUnreadableViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    ScanErrorScreen(
        titleResId = R.string.scanError_cardUnreadable_title,
        bodyResId = R.string.scanError_cardUnreadable_body,
        buttonTitleResId = R.string.scanError_close,
        showErrorCard = viewModel.errorCard,
        onNavigationButtonTapped = { viewModel.onCancelButtonPressed(context) },
        onButtonTapped = { viewModel.onCancelButtonPressed(context) }
    )
}

@HiltViewModel
class IdentificationCardUnreadableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coordinator: IdentificationCoordinator,
) : ViewModel() {
    val errorCard: Boolean
    val redirectUrl: String

    init {
        val args = IdentificationCardUnreadableDestination.argsFrom(savedStateHandle)
        errorCard = args.errorCard
        redirectUrl = args.redirectUrl
    }

    fun onCancelButtonPressed(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
        ContextCompat.startActivity(context, intent, null)

        coordinator.cancelIdentification()
    }
}

data class IdentificationCardUnreadableNavArgs(
    val errorCard: Boolean,
    val redirectUrl: String
)
