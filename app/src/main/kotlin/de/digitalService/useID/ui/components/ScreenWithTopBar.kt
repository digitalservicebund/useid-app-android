package de.digitalService.useID.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.dialogs.StandardDialog

enum class NavigationIcon {
    Cancel {
        @Composable
        override fun Icon() {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(id = R.string.navigation_cancel)
            )
        }
    },
    Back {
        @Composable
        override fun Icon() {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.navigation_back)
            )
        }
    };

    @Composable
    abstract fun Icon()
}

data class NavigationButton(
    val icon: NavigationIcon,
    val onClick: () -> Unit,
    val shouldShowConfirmDialog: Boolean = false,
    val isIdentification: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithTopBar(
    modifier: Modifier = Modifier,
    navigationButton: NavigationButton? = null,
    content: @Composable (topPadding: Dp) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { },
                navigationIcon = {
                    navigationButton?.let { navigationButton ->
                        IconButton(
                            modifier = Modifier.testTag(navigationButton.icon.name),
                            onClick = {
                                if (navigationButton.shouldShowConfirmDialog) {
                                    showConfirmDialog = true
                                } else {
                                    navigationButton.onClick()
                                }
                            },
                            content = { navigationButton.icon.Icon() }
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        content(paddingValues.calculateTopPadding())

        if (showConfirmDialog) {
            navigationButton?.let { navigationButton ->
                if (navigationButton.isIdentification) {
                    CancelDialog(
                        title = stringResource(R.string.identification_confirmEnd_title),
                        message = stringResource(R.string.identification_confirmEnd_message),
                        confirmButtonText = stringResource(id = R.string.identification_confirmEnd_confirm),
                        onConfirm = {
                            showConfirmDialog = false
                            navigationButton.onClick()
                        },
                        dismissButtonText = stringResource(id = R.string.identification_confirmEnd_deny),
                        onDismiss = { showConfirmDialog = false }
                    )
                } else {
                    CancelDialog(
                        title = stringResource(R.string.firstTimeUser_confirmEnd_title),
                        message = stringResource(R.string.firstTimeUser_confirmEnd_message),
                        confirmButtonText = stringResource(id = R.string.firstTimeUser_confirmEnd_confirm),
                        onConfirm = {
                            showConfirmDialog = false
                            navigationButton.onClick()
                        },
                        dismissButtonText = stringResource(id = R.string.firstTimeUser_confirmEnd_deny),
                        onDismiss = { showConfirmDialog = false }
                    )
                }
            }
        }
    }

    BackHandler(onBack = {
        navigationButton?.let { navigationButton ->
            if (!navigationButton.shouldShowConfirmDialog) {
                navigationButton.onClick()
                return@BackHandler
            }

            showConfirmDialog = true
        }
    })
}

@Composable
private fun CancelDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String,
    onDismiss: () -> Unit
) {
    StandardDialog(
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            BackHandler(onBack = onDismiss)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodySmall)
        },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onDismissButtonTap = onDismiss,
        onConfirmButtonTap = onConfirm
    )
}
