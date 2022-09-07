package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun DependenciesScreen() {
    var dialogText: Set<License>? by remember { mutableStateOf(null) }

    LibrariesContainer(Modifier.fillMaxSize()) {
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
                    Text(text = "Ok")
                }
            },
            containerColor = Color.White
        )
    }
}

@Preview
@Composable
private fun Preview() {
    DependenciesScreen()
}
