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
        private enum class Parameters {
            transportPIN
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.transportPIN -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun transportPIN(bundle: Bundle): String = bundle.getString(Parameters.transportPIN.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(transportPIN: String): String = "$screenName/$transportPIN"
    }

    object SetupPersonalPIN : Screen() {
        private enum class Parameters {
            transportPIN
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.transportPIN -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun transportPIN(bundle: Bundle): String = bundle.getString(Parameters.transportPIN.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(transportPIN: String): String = "$screenName/$transportPIN"
    }

    object SetupScan : Screen() {
        private enum class Parameters {
            transportPIN, personalPIN
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.transportPIN -> navArgument(it.name) { type = NavType.StringType }
                    Parameters.personalPIN -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun transportPIN(bundle: Bundle): String = bundle.getString(Parameters.transportPIN.name) ?: throw NavigationException.MissingArgumentException
        fun personalPIN(bundle: Bundle): String = bundle.getString(Parameters.personalPIN.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(transportPIN: String, personalPIN: String): String = "$screenName/$transportPIN/$personalPIN"
    }

    inline fun <reified T: Enum<T>> routeWithParameters(route: String): String = route + "/" + enumValues<T>().joinToString(
        "/"
    ) { "{${it.name}}" }
}