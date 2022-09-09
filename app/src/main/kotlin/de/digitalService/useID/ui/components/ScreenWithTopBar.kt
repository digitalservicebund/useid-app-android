package de.digitalService.useID.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import de.digitalService.useID.R

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

class NavigationButton(val icon: NavigationIcon, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithTopBar(
    modifier: Modifier = Modifier,
    navigationButton: NavigationButton? = null,
    content: @Composable (topPadding: Dp) -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { },
                navigationIcon = {
                    navigationButton?.let {
                        IconButton(
                            modifier = Modifier.testTag("navigationButton"),
                            onClick = navigationButton.onClick,
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
    }

    BackHandler(onBack = { navigationButton?.onClick?.invoke() })
}
