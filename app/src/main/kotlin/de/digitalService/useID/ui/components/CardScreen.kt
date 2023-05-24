package de.digitalService.useID.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

enum class CardScreenType {
    INFO, ERROR, SUCCESS
}

@Composable
fun CardScreen(
    type: CardScreenType,
    @StringRes title: Int,
    @StringRes cardText: Int,
    @StringRes buttonText: Int,
    confirmation: Flow?,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val imageResId: Int
    val cardColor: Color
    when (type) {
        CardScreenType.INFO -> {
            imageResId = R.drawable.img_info
            cardColor = UseIdTheme.colors.blue300
        }

        CardScreenType.ERROR -> {
            imageResId = R.drawable.img_error
            cardColor = UseIdTheme.colors.red200
        }

        CardScreenType.SUCCESS -> {
            imageResId = R.drawable.img_success
            cardColor = UseIdTheme.colors.green100
        }
    }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = onBack,
            confirmation = confirmation
        )
    ) { topPadding ->
        StandardButtonScreen(
            primaryButton = BundButtonConfig(
                title = stringResource(id = buttonText),
                action = onConfirm
            ),
            modifier = Modifier.padding(top = topPadding)
        ) { bottomPadding ->
            Column(
                modifier = Modifier
                    .padding(horizontal = UseIdTheme.spaces.m)
                    .padding(bottom = bottomPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(UseIdTheme.spaces.m)
                )

                Text(
                    stringResource(id = title),
                    style = UseIdTheme.typography.headingXl,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = UseIdTheme.shapes.roundedMedium
                ) {
                    Column(modifier = Modifier.padding(UseIdTheme.spaces.s)) {
                        MarkdownText(
                            markdown = markDownResource(id = cardText),
                            fontResource = R.font.bundes_sans_dtp_regular,
                            fontSize = UseIdTheme.typography.bodyLRegular.fontSize,
                            color = UseIdTheme.colors.black
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewSuccessCardScreen() {
    UseIdTheme {
        CardScreen(type = CardScreenType.SUCCESS,
                   title = R.string.firstTimeUser_alreadySetupConfirmation_title,
                   cardText = R.string.firstTimeUser_alreadySetupConfirmation_box,
                   buttonText = R.string.firstTimeUser_alreadySetupConfirmation_close,
                   confirmation = null,
                   onBack = {},
                   onConfirm = {})
    }
}

@Composable
@Preview
fun PreviewInfoCardScreen() {
    UseIdTheme {
        CardScreen(type = CardScreenType.INFO,
                   title = R.string.firstTimeUser_alreadySetupConfirmation_title,
                   cardText = R.string.firstTimeUser_alreadySetupConfirmation_box,
                   buttonText = R.string.firstTimeUser_alreadySetupConfirmation_close,
                   confirmation = null,
                   onBack = {},
                   onConfirm = {})
    }
}

@Composable
@Preview
fun PreviewErrorCardScreen() {
    UseIdTheme {
        CardScreen(type = CardScreenType.ERROR,
                   title = R.string.firstTimeUser_alreadySetupConfirmation_title,
                   cardText = R.string.firstTimeUser_alreadySetupConfirmation_box,
                   buttonText = R.string.firstTimeUser_alreadySetupConfirmation_close,
                   confirmation = null,
                   onBack = {},
                   onConfirm = {})
    }
}
