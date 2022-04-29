package com.example.idprototype

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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.idprototype.idCardInterface.IDCardAttribute
import com.example.idprototype.idCardInterface.EIDInteractionEvent
import com.example.idprototype.idCardInterface.IDCardManager
import com.example.idprototype.ui.theme.IDPrototypeTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

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
        val intent = Intent(activity.applicationContext, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        nfcAdapter?.enableForegroundDispatch(activity, nfcPendingIntent, null, null)

        // TODO: Disable foreground dispatch on finish
    }

    private fun identify(context: Context, pin: String) {
        val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"

        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.identify(context, urlString).collect { event ->
                when(event) {
                    EIDInteractionEvent.AuthenticationStarted -> {
                        Log.d("DEBUG", "Authentication started.")
                        MainScope().launch {
                            Toast.makeText(context, "Authentication started.", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Please keep ID card attached!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    EIDInteractionEvent.CardRemoved -> Log.d("DEBUG", "Card removed.")
                    is EIDInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        Log.d("DEBUG", "Confirm server data...")
                        event.confirmationCallback(IDCardAttribute.values().map { Pair(it, true) }.toMap())
                    }
                    EIDInteractionEvent.RequestCardInsertion -> {
                        Log.d("DEBUG", "Insert card!")
                        MainScope().launch {
                            Toast.makeText(context, "Please attach ID card to the device.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is EIDInteractionEvent.RequestPIN -> {
                        Log.d("DEBUG", "Enter PIN...")
                        event.pinCallback(pin)
                    }
                    else -> Log.e("DEBUG", "Collected unexpected event.")
                }
            }
            Log.d("DEBUG", "Start identification returned.")
        }

        Log.d("DEBUG", "Identification function returned.")
    }

    private fun pinManagement(context: Context, pin: String, newPIN: String) {
        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.changePin(context).collect { event ->
                when(event) {
                    EIDInteractionEvent.PINManagementStarted -> Log.d("DEBUG", "PIN management started.")
                    is EIDInteractionEvent.ProcessCompletedSuccessfully -> {
                        Log.d("DEBUG", "Process completed successfully.")
                        MainScope().launch {
                            Toast.makeText(context, "PIN changed successfully.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    EIDInteractionEvent.CardInteractionComplete -> {
                        Log.d("DEBUG", "Card interaction complete.")
                    }
                    EIDInteractionEvent.CardRecognized -> {
                        Log.d("DEBUG", "Card recognized.")
                        MainScope().launch {
                            Toast.makeText(context, "Please keep ID card attached!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    EIDInteractionEvent.CardRemoved -> Log.d("DEBUG", "Card removed.")
                    EIDInteractionEvent.RequestCardInsertion -> {
                        Log.d("DEBUG", "Insert card!")
                        MainScope().launch {
                            Toast.makeText(context, "Please attach ID card to the device.", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalComposeUiApi::class)
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
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
        ) {
            IDActionRow(
                action = {
                localSoftwareKeyboardController?.hide()
                    onIdentificationButtonClicked(identificationPINValue)
                         },
                actionLabel = "Identify",
                pinValueChanged = { identificationPINValue = it },
                additionalValueChanged = null
            )
            Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 50.dp))
            IDActionRow(
                action = {
                    localSoftwareKeyboardController?.hide()
                    onPINManagementButtonClicked(pinManagementPINValue, pinManagementNewPINValue)
                         },
                actionLabel = "Reset PIN",
                pinValueChanged = { pinManagementPINValue = it },
                additionalValueChanged = { pinManagementNewPINValue = it }
            )
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
    action: () -> Unit,
    actionLabel: String,
    pinValueChanged: (String) -> Unit,
    additionalValueChanged: ((String) -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        PINEntryField("PIN", onValueChanged = pinValueChanged)
        additionalValueChanged?.let {
            PINEntryField(label = "New PIN", onValueChanged = it)
        }
        Button(
            onClick = action,
            modifier = Modifier
                .width(140.dp)
                .padding(end = 10.dp)
        ) {
            Text(actionLabel)
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
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier
            .width(120.dp)
            .padding(10.dp)
    )
}

@Preview
@Composable
fun IDActionRowPreview() {
    IDActionRow(action = { }, actionLabel = "Test", pinValueChanged = { }, additionalValueChanged = null)
}

@Preview
@Composable
fun IDActionRowPreviewWithAdditionalValue() {
    IDActionRow(action = { }, actionLabel = "Test", pinValueChanged = { }, additionalValueChanged = {})
}