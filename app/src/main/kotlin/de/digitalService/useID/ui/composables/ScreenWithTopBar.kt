package de.digitalService.useID.ui.composables

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.screens.SetupTransportPIN

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
