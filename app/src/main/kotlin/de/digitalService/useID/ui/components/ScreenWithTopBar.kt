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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.theme.UseIdTheme

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

enum class Flow {
    Setup, Identification
}

data class NavigationButton(
    val icon: NavigationIcon,
    val onClick: () -> Unit,
    val confirmation: Flow?,
    val contentDescription: String? = null,
    val testTag: String? = null
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
        containerColor = UseIdTheme.colors.white,
        topBar = {
            SmallTopAppBar(
                title = { },
                navigationIcon = {
                    navigationButton?.let { navigationButton ->
                        IconButton(
                            modifier = Modifier
                                .semantics {
                                    testTag = navigationButton.testTag ?: navigationButton.icon.name
                                    navigationButton.contentDescription?.let {
                                        this.contentDescription = it
                                    }
                                },
                            onClick = {
                                if (navigationButton.confirmation != null) {
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
                    containerColor = UseIdTheme.colors.white,
                    navigationIconContentColor = UseIdTheme.colors.black
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        content(paddingValues.calculateTopPadding())

        if (showConfirmDialog) {
            navigationButton?.confirmation?.let { flow ->
                when (flow) {
                    Flow.Setup -> CancelDialog(
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
                    Flow.Identification -> CancelDialog(
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
                }
            }
        }
    }

    BackHandler(onBack = {
        navigationButton?.let {
            if (navigationButton.confirmation != null) {
                showConfirmDialog = true
            } else {
                navigationButton.onClick()
                return@BackHandler
            }
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
            Text(text = title, style = UseIdTheme.typography.headingL)

            BackHandler(onBack = onDismiss)
        },
        text = {
            Text(text = message, style = UseIdTheme.typography.bodyLRegular)
        },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onDismissButtonClick = onDismiss,
        onConfirmButtonClick = onConfirm
    )
}
