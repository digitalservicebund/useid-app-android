package de.digitalService.useID

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinator
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)

        intent.data?.let { uri ->
            val tcTokenURL = Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")
            appCoordinator.tcTokenURL = tcTokenURL
        }

        setContent {
            UseIDApp(appCoordinator, trackerManager)
        }
    }
}
