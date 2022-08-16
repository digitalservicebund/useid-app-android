@file:OptIn(ExperimentalMaterial3Api::class)

package de.digitalService.useID.ui.composables.screens.noNfc

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivity
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun NfcDeactivatedScreen() {
    Surface(shape = RoundedCornerShape(15.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(all = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(painter = painterResource(id = R.drawable.eids), contentDescription = "")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.nfcDeactivated_info_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.nfcDeactivated_info_body),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(64.dp))

                val context = LocalContext.current
                BundButton(
                    type = ButtonType.PRIMARY,
                    onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Intent(Settings.Panel.ACTION_NFC)
                        } else {
                            Intent(Settings.ACTION_SETTINGS)
                        }

                        startActivity(context, intent, null)
                    },
                    label = stringResource(R.string.ndcDeactivated_openSettings_button),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        NfcDeactivatedScreen()
    }
}
