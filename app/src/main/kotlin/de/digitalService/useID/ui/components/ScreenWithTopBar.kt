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
    val showConfirmDialog = remember { mutableStateOf(false) }

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
                                    showConfirmDialog.value = true
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

        if (showConfirmDialog.value) {
            navigationButton?.let { navigationButton ->
                if (navigationButton.isIdentification) {
                    IdentificationCancelDialog(navigationButton, showConfirmDialog)
                } else {
                    SetupCancelDialog(navigationButton, showConfirmDialog)
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

            showConfirmDialog.value = true
        }
    })
}

@Composable
private fun IdentificationCancelDialog(navigationButton: NavigationButton, showConfirmDialog: MutableState<Boolean>) {
    StandardDialog(
        title = {
            Text(
                text = stringResource(R.string.identification_confirmEnd_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            BackHandler(onBack = { showConfirmDialog.value = false })
        },
        text = {
            Text(
                text = stringResource(R.string.identification_confirmEnd_message),
                style = MaterialTheme.typography.bodySmall
            )
        },
        confirmButtonText = stringResource(id = R.string.identification_confirmEnd_confirm),
        dismissButtonText = stringResource(id = R.string.identification_confirmEnd_deny),
        onDismissButtonTap = { showConfirmDialog.value = false }
    ) {
        showConfirmDialog.value = false
        navigationButton.onClick()
    }
}

@Composable
private fun SetupCancelDialog(navigationButton: NavigationButton, showConfirmDialog: MutableState<Boolean>) {
    StandardDialog(
        title = {
            Text(
                text = stringResource(R.string.firstTimeUser_confirmEnd_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            BackHandler(onBack = { showConfirmDialog.value = false })
        },
        text = {
            Text(
                text = stringResource(R.string.firstTimeUser_confirmEnd_message),
                style = MaterialTheme.typography.bodySmall
            )
        },
        confirmButtonText = stringResource(id = R.string.firstTimeUser_confirmEnd_confirm),
        dismissButtonText = stringResource(id = R.string.firstTimeUser_confirmEnd_deny),
        onDismissButtonTap = { showConfirmDialog.value = false }
    ) {
        showConfirmDialog.value = false
        navigationButton.onClick()
    }
}
