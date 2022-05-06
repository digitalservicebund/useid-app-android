package de.digitalService.useID

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.theme.IDPrototypeTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private val idCardManager = IDCardManager()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleUI(
                onIdentificationButtonClicked = { pin ->
                    if (pin.length == 6) {
                        identify(this, pin)
                    } else {
                        Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                    }
                },
                onPINManagementButtonClicked = { pin, newPIN ->
                    if (pin.length == 6 && newPIN.length == 6) {
                        pinManagement(this, pin, newPIN)
                    } else {
                        Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()

        foregroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            Log.d("DEBUG", "Passing tag to IDCardManager.")
            idCardManager.handleNFCTag(tag)

        }
    }

    private fun foregroundDispatch(activity: Activity) {
        val intent = Intent(
            activity.applicationContext,
            activity.javaClass
        ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        nfcAdapter?.enableForegroundDispatch(activity, nfcPendingIntent, null, null)

        // TODO: Disable foreground dispatch on finish
    }

    private fun identify(context: Context, pin: String) {
        val urlString =
            "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"
//        val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.mtg.de%2Feid-soap-server%2FeIDSOAP%3Fapplicant%3D42CCFBA6EE0D4E1A"

        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.identify(context, urlString).onCompletion { error ->
                MainScope().launch {
                    if (error != null) {
                        if (error is IDCardInteractionException) {
                            when (error) {
                                is IDCardInteractionException.FrameworkError -> Toast.makeText(
                                    context,
                                    "Framework error: ${error.message}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                IDCardInteractionException.CardDeactivated -> Toast.makeText(
                                    context,
                                    "Error: ID card deactivated.",
                                    Toast.LENGTH_LONG
                                ).show()
                                IDCardInteractionException.CardBlocked -> Toast.makeText(
                                    context,
                                    "Error: ID card blocked.",
                                    Toast.LENGTH_LONG
                                ).show()
                                is IDCardInteractionException.ProcessFailed -> Toast.makeText(
                                    context,
                                    "Error: Process failed: ${error.resultCode.name}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                is IDCardInteractionException.UnexpectedReadAttribute -> Toast.makeText(
                                    context,
                                    "Error: Unexpected attribute to be read from ID card: ${error.message}.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${error.message} ${error.cause}.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.AuthenticationStarted -> {
                        Log.d("DEBUG", "Authentication started.")
                        MainScope().launch {
                            Toast.makeText(context, "Authentication started.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    EIDInteractionEvent.AuthenticationSuccessful -> {
                        Log.d("DEBUG", "Authentication successful.")
//                        MainScope().launch {
//                            Toast.makeText(context, "Authentication successful.", Toast.LENGTH_SHORT).show()
//                        }
                    }
                    is EIDInteractionEvent.ProcessCompletedSuccessfully -> {
                        Log.d("DEBUG", "Process completed successfully.")
                        event.redirect?.let {
                            Log.d("DEBUG", "Redirecting to URL $it")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            startActivity(intent)
                        }
                    }
                    EIDInteractionEvent.CardInteractionComplete -> {
                        Log.d("DEBUG", "Card interaction complete.")
                    }
                    EIDInteractionEvent.CardRecognized -> {
                        Log.d("DEBUG", "Card recognized.")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Please keep ID card attached!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    EIDInteractionEvent.CardRemoved -> {
                        Log.d("DEBUG", "Card removed.")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Disconnected from ID card.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is EIDInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        Log.d("DEBUG", "Confirm server data...")
                        event.confirmationCallback(IDCardAttribute.values().map { Pair(it, true) }
                            .toMap())
                    }
                    EIDInteractionEvent.RequestCardInsertion -> {
                        Log.d("DEBUG", "Insert card!")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Please attach ID card to the device.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is EIDInteractionEvent.RequestPIN -> {
                        Log.d("DEBUG", "Enter PIN...")
                        event.pinCallback(pin)
                    }
                    else -> Log.e("DEBUG", "Collected unexpected event: $event")
                }
            }
            Log.d("DEBUG", "Start identification returned.")
        }

        Log.d("DEBUG", "Identification function returned.")
    }

    private fun pinManagement(context: Context, pin: String, newPIN: String) {
        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.changePin(context).onCompletion { error ->
                if (error != null) {
                    MainScope().launch {
                        if (error is IDCardInteractionException) {
                            when (error) {
                                is IDCardInteractionException.FrameworkError -> Toast.makeText(
                                    context,
                                    "Framework error: ${error.message}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                IDCardInteractionException.CardDeactivated -> Toast.makeText(
                                    context,
                                    "Error: ID card deactivated.",
                                    Toast.LENGTH_LONG
                                ).show()
                                IDCardInteractionException.CardBlocked -> Toast.makeText(
                                    context,
                                    "Error: ID card blocked.",
                                    Toast.LENGTH_LONG
                                ).show()
                                is IDCardInteractionException.ProcessFailed -> Toast.makeText(
                                    context,
                                    "Error: Process failed: ${error.resultCode.name}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                is IDCardInteractionException.UnexpectedReadAttribute -> Toast.makeText(
                                    context,
                                    "Error: Unexpected attribute to be read from ID card: ${error.message}.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${error.message} ${error.cause}.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.PINManagementStarted -> Log.d(
                        "DEBUG",
                        "PIN management started."
                    )
                    is EIDInteractionEvent.ProcessCompletedSuccessfully -> {
                        Log.d("DEBUG", "Process completed successfully.")
                        MainScope().launch {
                            Toast.makeText(context, "PIN changed successfully.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    EIDInteractionEvent.CardInteractionComplete -> {
                        Log.d("DEBUG", "Card interaction complete.")
                    }
                    EIDInteractionEvent.CardRecognized -> {
                        Log.d("DEBUG", "Card recognized.")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Please keep ID card attached!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    EIDInteractionEvent.CardRemoved -> {
                        Log.d("DEBUG", "Card removed.")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Disconnected from ID card.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    EIDInteractionEvent.RequestCardInsertion -> {
                        Log.d("DEBUG", "Insert card!")
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Please attach ID card to the device.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is EIDInteractionEvent.RequestChangedPIN -> {
                        Log.d("DEBUG", "Entering old and new PIN...")
                        event.pinCallback(pin, newPIN)
                    }
                    else -> Log.e("DEBUG", "Collected unexpected event.")
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SimpleUI(
    onIdentificationButtonClicked: (String) -> Unit,
    onPINManagementButtonClicked: (String, String) -> Unit
) {
    var identificationPINValue by remember { mutableStateOf("") }
    var pinManagementPINValue by remember { mutableStateOf("") }
    var pinManagementNewPINValue by remember { mutableStateOf("") }

    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current

    IDPrototypeTheme {
        Scaffold {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ds_wortmarke),
                    contentDescription = "",
                    modifier = Modifier.padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.height(25.dp))
                Text("Identification\ntechnical prototype", style = MaterialTheme.typography.h1)
                Spacer(modifier = Modifier.height(25.dp))
                IDActionRow(
                    action = {
                        localSoftwareKeyboardController?.hide()
                        onIdentificationButtonClicked(identificationPINValue)
                    },
                    title = "Identification with test ID",
                    firstLabel = "Enter PIN",
                    actionLabel = "Identify",
                    pinValueChanged = { identificationPINValue = it },
                    additionalValueChanged = null
                )
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
                IDActionRow(
                    action = {
                        localSoftwareKeyboardController?.hide()
                        onPINManagementButtonClicked(
                            pinManagementPINValue,
                            pinManagementNewPINValue
                        )
                    },
                    title = "Change PIN with personal ID or test ID",
                    firstLabel = "Enter current PIN",
                    actionLabel = "Reset PIN",
                    pinValueChanged = { pinManagementPINValue = it },
                    additionalValueChanged = { pinManagementNewPINValue = it }
                )
            }
        }
    }
}

@Composable
@Preview
fun SimpleUIPreview() {
    SimpleUI(
        onIdentificationButtonClicked = { },
        onPINManagementButtonClicked = { _, _ -> }
    )
}

@Composable
fun IDActionRow(
    title: String,
    action: () -> Unit,
    actionLabel: String,
    firstLabel: String,
    pinValueChanged: (String) -> Unit,
    additionalValueChanged: ((String) -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(15.dp))
        PINEntryField(firstLabel, onValueChanged = pinValueChanged)
        additionalValueChanged?.let {
            PINEntryField(label = "Enter new PIN", onValueChanged = it)
        }
        Button(
            onClick = action,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(top = 20.dp)
        ) {
            Text(actionLabel, style = MaterialTheme.typography.button)
        }
    }
}

@Composable
fun PINEntryField(label: String, onValueChanged: (String) -> Unit) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.text.length <= 6) {
                value = it
                onValueChanged(it.text)
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(

        ),
        label = { Text(label, style = MaterialTheme.typography.subtitle1) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )
}

@Preview
@Composable
fun IDActionRowPreview() {
    IDActionRow(
        action = { },
        actionLabel = "Test",
        title = "Title",
        firstLabel = "Enter PIN",
        pinValueChanged = { },
        additionalValueChanged = null
    )
}

@Preview
@Composable
fun IDActionRowPreviewWithAdditionalValue() {
    IDActionRow(
        action = { },
        actionLabel = "Test",
        title = "Title",
        firstLabel = "Enter PIN",
        pinValueChanged = { },
        additionalValueChanged = {})
}