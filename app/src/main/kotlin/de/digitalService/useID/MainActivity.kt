package de.digitalService.useID

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UseIDApp()
        }
    }
}

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

enum class Screens {
    FIRST_TIME_USER_CHECK,
    FIRST_TIME_USER_PIN_LETTER_CHECK
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screens.FIRST_TIME_USER_CHECK.name, modifier = modifier) {
        composable(Screens.FIRST_TIME_USER_CHECK.name) {
            FirstTimeUserCheckScreen(firstTimeUserHandler = { }, experiencedUserHandler = { navController.navigate(Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name) })
        }

        composable(Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name) {
            FirstTimeUserPINLetterScreen()
        }
    }
}

@Composable
fun FirstTimeUserCheckScreen(firstTimeUserHandler: () -> Unit, experiencedUserHandler: () -> Unit) {
    OnboardingScreen(
        title = "Haben Sie Ihren Online-Ausweis bereits benutzt?",
        body = "Folgende Dokumente bieten die Funktion an:\nDeutscher Personalausweis, Elektronischer Aufenthaltstitel, eID-Karte für Unionsbürger",
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButtonAction = experiencedUserHandler,
        primaryButtonLabel = "Ja, ich habe es bereits genutzt",
        secondaryButtonAction = firstTimeUserHandler,
        secondaryButtonLabel = "Nein, jetzt Online-Ausweis einrichten"
    )
}

@Composable
fun FirstTimeUserPINLetterScreen() {
    OnboardingScreen(
        title = "Haben Sie noch Ihren PIN-Brief?",
        body = "Der PIN-Brief wurde Ihnen nach der Beantragung des Ausweises zugesandt.",
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButtonAction = { },
        primaryButtonLabel = "Ja, PIN-Brief vorhanden",
        secondaryButtonAction = { },
        secondaryButtonLabel = "Nein, neuen PIN-Brief bestellen"
    )
}

@Composable
fun OnboardingScreen(
    title: String,
    body: String,
    @DrawableRes imageID: Int,
    imageScaling: ContentScale,
    primaryButtonLabel: String,
    primaryButtonAction: () -> Unit,
    secondaryButtonLabel: String,
    secondaryButtonAction: () -> Unit
) {
    Column {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = imageID),
                contentScale = imageScaling,
                contentDescription = ""
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(20.dp)
        ) {
            BundButton(
                type = ButtonType.PRIMARY,
                onClick = primaryButtonAction,
                label = primaryButtonLabel
            )
            Spacer(modifier = Modifier.height(15.dp))
            BundButton(
                type = ButtonType.SECONDARY,
                onClick = secondaryButtonAction,
                label = secondaryButtonLabel
            )
        }
    }
}

enum class ButtonType {
    PRIMARY, SECONDARY
}

@Composable
fun BundButton(type: ButtonType, onClick: () -> Unit, label: String) {
    val containerColor: Color
    val contentColor: Color

    when (type) {
        ButtonType.PRIMARY -> {
            containerColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.SECONDARY -> {
            containerColor = MaterialTheme.colorScheme.secondary
            contentColor = MaterialTheme.colorScheme.onSecondary
        }
    }

    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ), shape = MaterialTheme.shapes.small, modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(label)
    }
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
fun PreviewUseIDApp() {
    UseIDApp()
}