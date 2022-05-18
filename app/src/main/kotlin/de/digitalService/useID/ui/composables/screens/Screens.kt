package de.digitalService.useID.ui.composables.screens

import android.os.Bundle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.digitalService.useID.ui.composables.NavigationException

sealed interface ScreenInterface {
    val screenName: String
        get() = this::class.simpleName!!

    val routeTemplate: String
        get() = screenName

    val namedNavArguments: List<NamedNavArgument>
        get() = listOf()
}

sealed class Screen: ScreenInterface {
    object FirstTimeUserCheck : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object FirstTimeUserPINLetterCheck : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object ResetPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object TransportPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetPINIntro : Screen() {
        private enum class Parameters {
            pin
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.pin -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun pin(bundle: Bundle): String = bundle.getString(Parameters.pin.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(pin: String): String = "$screenName/$pin"
    }

    inline fun <reified T: Enum<T>> routeWithParameters(route: String): String = route + "/" + enumValues<T>().joinToString(
        "/"
    ) { "{${it.name}}" }
}