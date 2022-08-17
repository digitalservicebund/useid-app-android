package de.digitalService.useID.ui.components

import android.graphics.Color
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import de.digitalService.useID.R

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            it.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            it.typeface = context.resources.getFont(R.font.bundes_sans_dtp_regular)
            it.setTextColor(Color.BLACK)
        }
    )
}
