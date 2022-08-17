package de.digitalService.useID.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithTopBar(navigationIcon: @Composable () -> Unit, modifier: Modifier = Modifier, content: @Composable (topPadding: Dp) -> Unit) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { },
                navigationIcon = { navigationIcon() },
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
}
