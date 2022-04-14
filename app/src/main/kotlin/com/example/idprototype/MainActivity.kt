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
import com.example.idprototype.ui.theme.IDPrototypeTheme
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.common.OpenecardProperties
import org.openecard.mobile.activation.*
import org.openecard.mobile.ui.BoxItemImpl

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var androidContextManager: AndroidContextManager? = null

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

        if (androidContextManager != null) {
            Log.d("DEBUG", "Android context manager is set. Handing intent over.")
            androidContextManager?.onNewIntent(intent)
            return
        }

        var resultString = ""

        val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val nfc = IsoDep.get(tagFromIntent) //NfcA.get(tagFromIntent)

        nfc.connect()
        if (nfc.isConnected) {
            Log.d("DEBUG", "Connected to tag.")
            val maxLength = nfc.maxTransceiveLength
            Log.d("DEBUG", "APDU length: $maxLength.")
            val extendedSupported = nfc.isExtendedLengthApduSupported
            Log.d("DEBUG", "APDU extended length supported: $extendedSupported.")
        } else {
            Log.d("DEBUG", "Not connected to tag.")
        }

        nfc.close()

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
        val openECard = OpeneCard.createInstance()
        androidContextManager = openECard.context(context)

        androidContextManager!!.initializeContext(object: StartServiceHandler {
            override fun onSuccess(p0: ActivationSource?) {
                Log.d("DEBUG", "Initialized framework successfully.")
                val factory = p0!!
//                val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"
                val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"
//                val urlString = "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3a%2f%2feid.mtg.de%3a443%2feid-server-demo-app%2fresult%2fgetTcToken.html%3bjsessionid%3d575FC2B3C46D0E3CBD7678F057C6A722"
                val eacActivationController = factory.eacFactory().create(urlString, object: ControllerCallback {
                    override fun onStarted() {
                        Log.d("DEBUG", "EAC started.")
                    }

                    override fun onAuthenticationCompletion(p0: ActivationResult?) {
                        Log.d("DEBUG", "EAC complete.")
                    }
                }, object: EacInteraction {
                    override fun requestCardInsertion() {
                        Log.d("DEBUG", "requestCardInsertion()")
                    }

                    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
                        Log.d("DEBUG", "requestCardInsertion(p0: NFCOverlayMessageHandler?)")
                    }

                    override fun onCardInteractionComplete() {
                        Log.d("DEBUG", "onCardInteractionComplete()")
                    }

                    override fun onCardRecognized() {
                        Log.d("DEBUG", "onCardRecognized()")
                    }

                    override fun onCardRemoved() {
                        Log.d("DEBUG", "onCardRemoved()")
                    }

                    override fun onCanRequest(p0: ConfirmPasswordOperation?) {
                        Log.d("DEBUG", "onCanRequest(p0: ConfirmPasswordOperation?)")
                    }

                    override fun onPinRequest(p0: ConfirmPasswordOperation?) {
                        Log.d("DEBUG", "onPinRequest(p0: ConfirmPasswordOperation?)")
                        p0!!.confirmPassword("123456")
                    }

                    override fun onPinRequest(p0: Int, p1: ConfirmPasswordOperation?) {
                        Log.d("DEBUG", "onPinRequest(p0: $p0, p1: ConfirmPasswordOperation?)")
                    }

                    override fun onPinCanRequest(p0: ConfirmPinCanOperation?) {
                        Log.d("DEBUG", "onPinCanRequest(p0: ConfirmPinCanOperation?)")
                    }

                    override fun onCardBlocked() {
                        Log.d("DEBUG", "onCardBlocked()")
                    }

                    override fun onCardDeactivated() {
                        Log.d("DEBUG", "onCardDeactivated()")
                    }

                    override fun onServerData(
                        p0: ServerData?,
                        p1: String?,
                        p2: ConfirmAttributeSelectionOperation?
                    ) {
                        Log.d("DEBUG", "onServerData: " +
                                "issuer: ${p0!!.issuer}\n" +
                                "issuerURL: ${p0!!.issuerUrl}\n" +
                                "subject: ${p0!!.subject}\n" +
                                "subjectURL: ${p0!!.subjectUrl}\n" +
                                "termsOfUsage: ${p0!!.termsOfUsage}\n" +
                                "validity: ${p0!!.validity}\n" +
                                "readAttributes: ${p0!!.readAccessAttributes}\n" +
                                "writeAttributes: ${p0!!.writeAccessAttributes}\n"
                        )
                        val readableItem = BoxItemImpl("DG01", true, false, "Dokumentenart")
                        p2!!.enterAttributeSelection(listOf(readableItem), listOf())
                    }

                    override fun onCardAuthenticationSuccessful() {
                        Log.d("DEBUG", "onCardAuthenticationSuccessful()")
                    }

                })

//                Log.d("DEBUG", "Shutting down framework.")
//                androidContextManager.terminateContext(object: StopServiceHandler {
//                    override fun onSuccess() {
//                        Log.d("DEBUG", "Terminated framework successfully.")
//                    }
//
//                    override fun onFailure(p0: ServiceErrorResponse?) {
//                        Log.d("DEBUG", "Failed to terminate framework.")
//                    }
//
//                })
            }

            override fun onFailure(p0: ServiceErrorResponse?) {
                Log.d("DEBUG", "Failed to initialize framework.")
            }
        })
    }

    private fun pinManagement(context: Context) {
        val openECard = OpeneCard.createInstance()
        androidContextManager = openECard.context(context)

        androidContextManager!!.initializeContext(object: StartServiceHandler {
            override fun onSuccess(p0: ActivationSource?) {
                Log.d("DEBUG", "Initialized framework successfully.")
                val factory = p0!!
                val pinManagementController = p0.pinManagementFactory().create(object: ControllerCallback {
                    override fun onStarted() {
                        Log.d("DEBUG", "PIN management started.")
                    }

                    override fun onAuthenticationCompletion(p0: ActivationResult?) {
                        Log.d("DEBUG", "PIN management completed.")
                    }
                }, object: PinManagementInteraction {
                    override fun requestCardInsertion() {
                        Log.d("DEBUG", "requestCardInsertion()")
                    }

                    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
                        Log.d("DEBUG", "requestCardInsertion(p0: NFCOverlayMessageHandler?)")
                    }

                    override fun onCardInteractionComplete() {
                        Log.d("DEBUG", "onCardInteractionComplete()")
                    }

                    override fun onCardRecognized() {
                        Log.d("DEBUG", "onCardRecognized()")
                    }

                    override fun onCardRemoved() {
                        Log.d("DEBUG", "onCardRemoved()")
                    }

                    override fun onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?) {
                        Log.d("DEBUG", "onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?)")
                        p0!!.confirmPassword("123456", "000000")
                    }

                    override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
                        Log.d("DEBUG", "onPinChangeable(p0: $p0, p1: ConfirmOldSetNewPasswordOperation?)")
                        p1!!.confirmPassword("123456", "000000")
                    }

                    override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
                        Log.d("DEBUG", "onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?)")
                    }

                    override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
                        Log.d("DEBUG", "onPinBlocked()")
                    }

                    override fun onCardPukBlocked() {
                        Log.d("DEBUG", "onCardPukBlocked()")
                    }

                    override fun onCardDeactivated() {
                        Log.d("DEBUG", "onCardDeactivated()")
                    }

                })
            }

            override fun onFailure(p0: ServiceErrorResponse?) {
                Log.d("DEBUG", "Failed to initialize framework.")
            }
        })
    }
}