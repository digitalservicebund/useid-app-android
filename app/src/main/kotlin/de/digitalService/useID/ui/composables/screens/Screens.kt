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

    inline fun <reified T : Enum<T>> routeWithParameters(route: String): String = route + "/" + enumValues<T>().joinToString(
        "/"
    ) { "{${it.name}}" }
}
