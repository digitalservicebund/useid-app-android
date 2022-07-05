package de.digitalService.useID.ui.composables.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.digitalService.useID.R
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.ScanErrorAlertDialog

@Composable
fun ScanScreen(
    title: String,
    body: String,
    errorState: ScanError?,
    onIncorrectPIN: @Composable (Int) -> Unit,
    onCancel: () -> Unit,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    errorState?.let { error ->
        when (error) {
            is ScanError.IncorrectPIN -> onIncorrectPIN(error.attempts)
            else -> ScanErrorAlertDialog(error = error, onButtonTap = onCancel)
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.eids),
            contentScale = ContentScale.Fit,
            contentDescription = ""
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            body,
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .height(40.dp)
        ) {
            Text(stringResource(id = R.string.firstTimeUser_scan_helpButton))
        }
    }

    AnimatedVisibility(visible = showProgress) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}
