package de.digitalService.useID.ui.composables

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.rememberNavController
import de.digitalService.useID.R
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import io.sentry.compose.withSentryObservableEffect

@Composable
fun UseIDApp(appCoordinator: AppCoordinator) {
    val navController = rememberNavController().withSentryObservableEffect()
    var shouldShowBackButton by remember { mutableStateOf(false) }

    appCoordinator.setNavController(navController)

    navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            shouldShowBackButton = controller.previousBackStackEntry != null
        }
    })

    UseIDTheme {
        ScreenWithTopBar(navigationIcon = {
            if (shouldShowBackButton) {
                IconButton(modifier = Modifier.testTag("backButton"), onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigation_back)
                    )
                }
            }
        }) { topBarPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(top = topBarPadding)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
fun PreviewUseIDApp() {
    UseIDApp(appCoordinator = AppCoordinator())
}
