package de.digitalService.useID.util

import de.digitalService.useID.flows.CanStateMachine
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class CanPinManagementStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): CanStateMachine.State {
        return when (what) {
            CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

            CanStateMachine.State.ChangePin.Intro::class -> CanStateMachine.State.ChangePin.Intro(false, "", "")
            CanStateMachine.State.ChangePin.IdAlreadySetup::class -> CanStateMachine.State.ChangePin.IdAlreadySetup(false, "", "")
            CanStateMachine.State.ChangePin.PinReset::class -> CanStateMachine.State.ChangePin.PinReset(false, "", "")
            CanStateMachine.State.ChangePin.CanIntro::class -> CanStateMachine.State.ChangePin.CanIntro(false, "", "", false)
            CanStateMachine.State.ChangePin.CanInput::class -> CanStateMachine.State.ChangePin.CanInput(false, "", "", false)
            CanStateMachine.State.ChangePin.CanInputRetry::class -> CanStateMachine.State.ChangePin.CanInputRetry(false, "", "")
            CanStateMachine.State.ChangePin.PinInput::class -> CanStateMachine.State.ChangePin.PinInput(false, "", "", "")
            CanStateMachine.State.ChangePin.CanAndPinEntered::class -> CanStateMachine.State.ChangePin.CanAndPinEntered(false, "", "", "")
            CanStateMachine.State.ChangePin.FrameworkReadyForPinInput::class -> CanStateMachine.State.ChangePin.FrameworkReadyForPinInput(false, "", "")
            CanStateMachine.State.ChangePin.FrameworkReadyForNewPinInput::class -> CanStateMachine.State.ChangePin.FrameworkReadyForNewPinInput(false, "", "")

            else -> throw IllegalArgumentException()
        }
    }
}

class CanIdentStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): CanStateMachine.State {
        return when (what) {
            CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

            CanStateMachine.State.Ident.Intro::class -> CanStateMachine.State.Ident.Intro(null)
            CanStateMachine.State.Ident.PinReset::class -> CanStateMachine.State.Ident.PinReset(null)
            CanStateMachine.State.Ident.CanIntro::class -> CanStateMachine.State.Ident.CanIntro(null, false)
            CanStateMachine.State.Ident.CanInput::class -> CanStateMachine.State.Ident.CanInput(null, false)
            CanStateMachine.State.Ident.CanInputRetry::class -> CanStateMachine.State.Ident.CanInputRetry("", false)
            CanStateMachine.State.Ident.PinInput::class -> CanStateMachine.State.Ident.PinInput("", false)
            CanStateMachine.State.Ident.CanAndPinEntered::class -> CanStateMachine.State.Ident.CanAndPinEntered("", "", false)
            CanStateMachine.State.Ident.FrameworkReadyForPinInput::class -> CanStateMachine.State.Ident.FrameworkReadyForPinInput("")

            else -> throw IllegalArgumentException()
        }
    }
}
