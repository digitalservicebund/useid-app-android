package de.digitalService.useID.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.screens.destinations.WebViewScreenDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(
    navArgsDelegate = WebViewNavArgs::class
)
@Composable
fun WebViewScreen(viewModel: WebViewScreenViewModelInterface = hiltViewModel<WebViewScreenViewModel>()) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        return if (request.url.getQueryParameter("tcTokenURL") != null) {
                            viewModel.startIdentification(request.url)
                            true
                        } else {
                            false
                        }
                    }
                }

                val logger by getLogger()
                logger.debug("height $height")



                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

//            loadUrl("https://www.eservice-drv.de/OnlineDiensteWeb/init.do?npa=true#")
//            loadUrl("https://www.kba-online.de/registerauskunft/ora/web/?#/faer")
//                loadUrl("https://demo.useid.dev.ds4g.net/?view=app")
                loadUrl(viewModel.url)
            }
        })

}

data class WebViewNavArgs(
    val url: String
)

interface WebViewScreenViewModelInterface {
    val url: String
    fun startIdentification(url: Uri)
}

@HiltViewModel
class WebViewScreenViewModel @Inject constructor(
    private val appCoordinator: AppCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), WebViewScreenViewModelInterface {

    override val url: String

    init {
        val args = WebViewScreenDestination.argsFrom(savedStateHandle)
        url = args.url
    }

    override fun startIdentification(url: Uri) {
        appCoordinator.handleDeepLink(url)
    }
}

class PreviewWebViewScreenViewModel : WebViewScreenViewModelInterface {
    override val url: String = ""

    override fun startIdentification(url: Uri) {}
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        WebViewScreen(PreviewWebViewScreenViewModel())
    }
}
