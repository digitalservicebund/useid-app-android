package com.example.idprototype

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.idprototype.idCardInterface.Attribute
import com.example.idprototype.idCardInterface.Event
import com.example.idprototype.idCardInterface.IDCardManager
import com.example.idprototype.ui.theme.IDPrototypeTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.common.OpenecardProperties
import org.openecard.mobile.activation.*
import org.openecard.mobile.ui.BoxItemImpl
import kotlin.coroutines.coroutineContext

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
//    private var androidContextManager: AndroidContextManager? = null
    private val idCardManager = IDCardManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IDPrototypeTheme {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                ) {
                    Button(
                        onClick = { checkNFC(this@MainActivity) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(5.dp)
                    ) {
                        Text("Check NFC")
                    }

                    Button(
                        onClick = { checkIDCard(this@MainActivity) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(5.dp)
                    ) {
                        Text("Check ID card")
                    }

                    Button(
                        onClick = { identify(this@MainActivity) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(5.dp)
                    ) {
                        Text("Identify")
                    }

                    Button(
                        onClick = { pinManagement(this@MainActivity) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(5.dp)
                    ) {
                        Text("Change PIN")
                    }
                }
            }
        }

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()

        foregroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("DEBUG", "On new intent!")

        intent?.let { idCardManager.handleNFCIntent(it) }

//        if (androidContextManager != null) {
//            Log.d("DEBUG", "Android context manager is set. Handing intent over.")
//            androidContextManager?.onNewIntent(intent)
//            return
//        }
//
//        var resultString = ""
//
//        val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//        val nfc = IsoDep.get(tagFromIntent) //NfcA.get(tagFromIntent)
//
//        nfc.connect()
//        if (nfc.isConnected) {
//            Log.d("DEBUG", "Connected to tag.")
//            val maxLength = nfc.maxTransceiveLength
//            Log.d("DEBUG", "APDU length: $maxLength.")
//            val extendedSupported = nfc.isExtendedLengthApduSupported
//            Log.d("DEBUG", "APDU extended length supported: $extendedSupported.")
//        } else {
//            Log.d("DEBUG", "Not connected to tag.")
//        }
//
//        nfc.close()

        Log.d("DEBUG", "On new intent done.")
    }

    private fun foregroundDispatch(activity: Activity) {
        val intent = Intent(activity.applicationContext, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        nfcAdapter?.enableForegroundDispatch(activity, nfcPendingIntent, null, null)
    }

    private fun checkNFC(context: Context) {
        val enabled = nfcAdapter!!.isEnabled
        Toast.makeText(context, "NFC supported: $enabled", Toast.LENGTH_LONG).show()
    }

    private fun checkIDCard(activity: Activity) {
        foregroundDispatch(activity)
        Toast.makeText(activity.applicationContext, "Hold ID card near device.", Toast.LENGTH_SHORT).show()
    }

    private fun identify(context: Context) {
        val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"

        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.startIdentification(context, urlString).collect { event ->
                when(event) {
                    Event.AuthenticationSuccessful -> Log.d("DEBUG", "Authentication successful.")
                    Event.CardInteractionComplete -> Log.d("DEBUG", "Card interaction complete.")
                    Event.CardRecognized -> Log.d("DEBUG", "Card recognized.")
                    Event.CardRemoved -> Log.d("DEBUG", "Card removed.")
                    is Event.RequestAuthenticationRequestConfirmation -> {
                        Log.d("DEBUG", "Confirm server data...")
                        event.confirmationCallback(Attribute.values().map { Pair(it, true) }.toMap())
                    }
                    Event.RequestCardInsertion -> Log.d("DEBUG", "Insert card!")
                    is Event.RequestPIN -> {
                        Log.d("DEBUG", "Entering PIN...")
                        event.pinCallback("123456")
                    }
                }
            }
            Log.d("DEBUG", "Start identification returned.")
        }

        Log.d("DEBUG", "Identification function returned.")
    }

    private fun pinManagement(context: Context) {

    }
}