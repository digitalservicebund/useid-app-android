package de.digitalService.useID.ui.composables

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.rememberNavController
import de.digitalService.useID.ui.theme.UseIDTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseIDApp() {
    val navController = rememberNavController()
    var shouldShowBackButton by remember { mutableStateOf(false) }

    navController.addOnDestinationChangedListener(object: NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            shouldShowBackButton = controller.previousBackStackEntry != null
        }
    })

    UseIDTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { },
                    navigationIcon = {
                        if (shouldShowBackButton) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            AppNavHost(navController = navController, modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxWidth())
        }
    }
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
fun PreviewUseIDApp() {
    UseIDApp()
}