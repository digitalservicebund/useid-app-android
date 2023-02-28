package de.digitalService.useID.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme
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
        containerColor = UseIdTheme.colors.white,
        bottomBar = {
            Column(
                horizontalAlignment = CenterHorizontally,
                modifier = Modifier
                    .padding(UseIdTheme.spaces.m)
            ) {
                primaryButton?.let {
                    BundButton(
                        type = ButtonType.PRIMARY,
                        onClick = it.action,
                        label = it.title
                    )
                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))
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
    @DrawableRes imageId: Int? = null,
    imageScaling: ContentScale = ContentScale.Inside,
    imageModifier: Modifier = Modifier,
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
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(bottom = bottomPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                title,
                style = UseIdTheme.typography.headingXl,
                color = UseIdTheme.colors.black,
                modifier = Modifier.semantics { heading() }
            )
            body?.let {
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                MarkdownText(
                    body,
                    style = UseIdTheme.typography.bodyLRegular,
                    fontResource = R.font.bundes_sans_dtp_regular,
                    fontSize = UseIdTheme.typography.bodyLRegular.fontSize
                )
            }

            imageId?.let {
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Image(
                    painter = painterResource(id = imageId),
                    contentScale = imageScaling,
                    contentDescription = "",
                    modifier = imageModifier.align(CenterHorizontally).semantics { testTag = imageId.toString() }
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenTwoButtons() {
    UseIdTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageId = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig("Primary button", { }),
            secondaryButton = BundButtonConfig("Secondary button", { })
        )
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenOneButton() {
    UseIdTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageId = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig("Primary button", { }),
            secondaryButton = null
        )
    }
}

@Composable
@Preview
fun PreviewOnboardingScreenNoButton() {
    UseIdTheme {
        StandardStaticComposition(
            title = "Title",
            body = "Body",
            imageId = R.drawable.eid_3,
            imageScaling = ContentScale.FillWidth,
            primaryButton = null,
            secondaryButton = null
        )
    }
}
