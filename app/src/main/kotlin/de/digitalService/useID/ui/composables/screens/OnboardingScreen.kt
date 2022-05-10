package de.digitalService.useID.ui.composables.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.theme.UseIDTheme

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

@Composable
@Preview
fun PreviewOnboardingScreen() {
    UseIDTheme {
        OnboardingScreen(
            title = "Title",
            body = "Body",
            imageID = R.drawable.eids,
            imageScaling = ContentScale.FillWidth,
            primaryButtonLabel = "Primary Button",
            primaryButtonAction = { },
            secondaryButtonLabel = "Secondary Button",
            secondaryButtonAction = { }
        )
    }
}