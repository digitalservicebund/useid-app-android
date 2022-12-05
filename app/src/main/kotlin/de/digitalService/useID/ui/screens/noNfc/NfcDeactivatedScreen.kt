package de.digitalService.useID.ui.screens.noNfc

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivity
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButton
import de.digitalService.useID.ui.components.ButtonType
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun NfcDeactivatedScreen() {
    Surface(shape = UseIdTheme.shapes.roundedLarge) {
        Scaffold(
            bottomBar = {
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
                    modifier = Modifier.fillMaxWidth().padding(UseIdTheme.spaces.m)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(bottom = it.calculateBottomPadding())
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.illustration_no_nfc),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(vertical = 40.dp)
                        .align(CenterHorizontally)
                )

                Text(
                    text = stringResource(R.string.nfcDeactivated_info_title),
                    style = UseIdTheme.typography.headingL,
                    color = UseIdTheme.colors.black
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

                Text(
                    text = stringResource(R.string.nfcDeactivated_info_body),
                    style = UseIdTheme.typography.bodyLRegular,
                    color = UseIdTheme.colors.black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        NfcDeactivatedScreen()
    }
}
