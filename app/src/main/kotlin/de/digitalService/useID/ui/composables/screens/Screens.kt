package de.digitalService.useID.ui.composables.screens

import androidx.lifecycle.SavedStateHandle
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
        private enum class Parameters {
            Attempts
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.Attempts -> navArgument(it.name) { type = NavType.IntType }
                }
            }

        fun attempts(savedStateHandle: SavedStateHandle): Int = savedStateHandle.get(Parameters.Attempts.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(attempts: Int): String = "$screenName/$attempts"
    }

    object SetupPersonalPINIntro : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupPersonalPIN : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    object SetupScan : Screen() {
        private enum class Parameters {
            TransportPIN, PersonalPIN
        }

        override val routeTemplate: String
            get() = routeWithParameters<Parameters>(screenName)

        override val namedNavArguments: List<NamedNavArgument>
            get() = Parameters.values().map {
                when (it) {
                    Parameters.TransportPIN -> navArgument(it.name) { type = NavType.StringType }
                    Parameters.PersonalPIN -> navArgument(it.name) { type = NavType.StringType }
                }
            }

        fun transportPIN(savedStateHandle: SavedStateHandle): String = savedStateHandle.get(Parameters.TransportPIN.name) ?: throw NavigationException.MissingArgumentException
        fun personalPIN(savedStateHandle: SavedStateHandle): String = savedStateHandle.get(Parameters.PersonalPIN.name) ?: throw NavigationException.MissingArgumentException

        fun parameterizedRoute(transportPIN: String, personalPIN: String): String = "$screenName/$transportPIN/$personalPIN"
    }

    object SetupFinish : Screen() {
        fun parameterizedRoute(): String = screenName
    }

    inline fun <reified T : Enum<T>> routeWithParameters(route: String): String = route + "/" + enumValues<T>().joinToString(
        "/"
    ) { "{${it.name}}" }
}
