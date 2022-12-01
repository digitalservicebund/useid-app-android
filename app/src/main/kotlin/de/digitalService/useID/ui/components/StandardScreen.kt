package de.digitalService.useID.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

class BundButtonConfig(
    val title: String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardButtonScreen(
    modifier: Modifier = Modifier,
    primaryButton: BundButtonConfig? = null,
    secondaryButton: BundButtonConfig? = null,
    content: @Composable (Dp) -> Unit
) {
    Scaffold(
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
            ) {
                primaryButton?.let {
                    BundButton(
                        type = ButtonType.PRIMARY,
                        onClick = it.action,
                        label = it.title
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
                secondaryButton?.let {
                    BundButton(
                        type = ButtonType.SECONDARY,
                        onClick = it.action,
                        label = it.title
                    )
                }
            }
        },
        modifier = modifier
    ) {
        content(it.calculateBottomPadding())
    }
}

@Composable
fun StandardStaticComposition(
    title: String,
    body: String?,
    @DrawableRes imageID: Int,
    imageScaling: ContentScale,
    modifier: Modifier = Modifier,
    primaryButton: BundButtonConfig? = null,
    secondaryButton: BundButtonConfig? = null
) {
    StandardButtonScreen(
        modifier = modifier,
        primaryButton = primaryButton,
        secondaryButton = secondaryButton
    ) { bottomPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = bottomPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge
            )
            body?.let {
                Spacer(modifier = Modifier.height(20.dp))
                MarkdownText(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    fontResource = R.font.bundes_sans_dtp_regular
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = imageID),
                contentScale = imageScaling,
                contentDescription = "",
                modifier = Modifier.align(CenterHorizontally)
            )
        }
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenTwoButtons() {
    UseIDTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageID = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig("Primary button", { }),
            secondaryButton = BundButtonConfig("Secondary button", { })
        )
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenOneButton() {
    UseIDTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageID = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig("Primary button", { }),
            secondaryButton = null
        )
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenNoButton() {
    UseIDTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageID = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = null,
            secondaryButton = null
        )
    }
}
