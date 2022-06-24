package de.digitalService.useID.ui.composables.screens

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.composables.NavigationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface ScreenInterface {
    val screenName: String
        get() = this::class.simpleName!!

    val routeTemplate: String
        get() = screenName

    val namedNavArguments: List<NamedNavArgument>
        get() = listOf()
}

sealed class Screen : ScreenInterface {
    object SetupIntro : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupPINLetter : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object ResetPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupTransportPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupPersonalPINIntro : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupPersonalPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupScan : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupFinish : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object IdentificationFetchMetadata : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object IdentificationAttributeConsent : Screen() {
        private enum class Parameters {
            Request
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.Request -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun request(savedStateHandle: SavedStateHandle): EIDAuthenticationRequest {
            val uriEncodedRequest = savedStateHandle.get<String>(Parameters.Request.name) ?: throw NavigationException.MissingArgumentException
            val jsonEncodedRequest = Uri.decode(uriEncodedRequest)
            return Json.decodeFromString(jsonEncodedRequest)
        }

        fun parameterizedRoute(request: EIDAuthenticationRequest): String {
            val jsonEncodedRequest = Json.encodeToString(request)
            val uriEncodedRequest = Uri.encode(jsonEncodedRequest)
            return "$screenName/$uriEncodedRequest"
        }
    }

    inline fun <reified T : Enum<T>> routeWithParameters(route: String): String = route + "/" + enumValues<T>().joinToString(
        "/"
    ) { "{${it.name}}" }
}
