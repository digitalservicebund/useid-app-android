package de.digitalService.useID.ui.composables.screens.information

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Destination
@Composable
fun AccessibilityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HtmlText(html = stringResource(id = R.string.accessibility_html_text))
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            it.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            it.typeface = context.resources.getFont(R.font.bundes_sans_dtp_regular)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        AccessibilityScreen()
    }
}
