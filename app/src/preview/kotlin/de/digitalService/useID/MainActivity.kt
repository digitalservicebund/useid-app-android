package de.digitalService.useID

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinator
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appCoordinator: AppCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.let { uri ->
            val tcTokenURL = Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")
            appCoordinator.tcTokenURL = tcTokenURL
        }

        setContent {
            UseIDApp(appCoordinator)
        }
    }
}
