package de.digitalService.useID

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.UseIDApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appCoordinator: AppCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UseIDApp(appCoordinator)
        }
    }
}
