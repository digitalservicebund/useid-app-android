package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar

@Destination
@Composable
fun DependenciesScreen(
    navigator: DestinationsNavigator
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = { navigator.navigateUp() })
    ) { topPadding ->
        var dialogText: Set<License>? by remember { mutableStateOf(null) }

        LibrariesContainer(
            Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            dialogText = it.licenses
        }

        dialogText?.let {
            AlertDialog(
                onDismissRequest = { dialogText = null },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        it.forEach {
                            Text(text = "${it.name}, ${it.year}\n ${it.licenseContent}")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { dialogText = null }) {
                        Text(text = stringResource(R.string.licenseDialog_confirm))
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DependenciesScreen(EmptyDestinationsNavigator)
}
