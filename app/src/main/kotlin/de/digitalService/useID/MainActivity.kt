package de.digitalService.useID

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.ui.theme.UseIDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UseIDApp()
        }
    }
}

@Composable
fun UseIDApp() {
    UseIDTheme {
        Scaffold {
            Column {
                Text("Back")
                Text("Haben Sie Ihren Ausweis bereits benutzt?")
                Text("Folgende Dokumente bieten die Funktion an: ...")
                Text("#Image")
                Button(onClick = { }){
                    Text("Ja, ich habe es bereits genutzt")
                }
                Button(onClick = { }){
                    Text("Nein, jetzt Online-Ausweis einrichten")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewUseIDApp() {
    UseIDApp()
}