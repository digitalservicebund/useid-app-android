package de.digitalService.useID.util

import de.digitalService.useID.flows.CanStateMachine
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class CanPinManagementStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): CanStateMachine.State {
        return when (what) {
            CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

            CanStateMachine.State.PinManagement.Intro::class -> CanStateMachine.State.PinManagement.Intro({ _, _, _ -> }, "", "")
            CanStateMachine.State.PinManagement.IdAlreadySetup::class -> CanStateMachine.State.PinManagement.IdAlreadySetup({ _, _, _ -> }, "", "")
            CanStateMachine.State.PinManagement.PinReset::class -> CanStateMachine.State.PinManagement.PinReset({ _, _, _ -> }, "", "")
            CanStateMachine.State.PinManagement.CanIntro::class -> CanStateMachine.State.PinManagement.CanIntro({ _, _, _ -> }, "", "", false)
            CanStateMachine.State.PinManagement.CanInput::class -> CanStateMachine.State.PinManagement.CanInput({ _, _, _ -> }, "", "", false)
            CanStateMachine.State.PinManagement.CanInputRetry::class -> CanStateMachine.State.PinManagement.CanInputRetry({ _, _, _ -> }, "", "")
            CanStateMachine.State.PinManagement.PinInput::class -> CanStateMachine.State.PinManagement.PinInput({ _, _, _ -> }, "", "", "")
            CanStateMachine.State.PinManagement.CanAndPinEntered::class -> CanStateMachine.State.PinManagement.CanAndPinEntered({ _, _, _ -> }, "", "", "")

            else -> throw IllegalArgumentException()
        }
    }
}

class CanIdentStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): CanStateMachine.State {
        return when (what) {
            CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

            CanStateMachine.State.Ident.Intro::class -> CanStateMachine.State.Ident.Intro({ _, _ -> }, null)
            CanStateMachine.State.Ident.PinReset::class -> CanStateMachine.State.Ident.PinReset({ _, _ -> }, null)
            CanStateMachine.State.Ident.CanIntro::class -> CanStateMachine.State.Ident.CanIntro({ _, _ -> }, null)
            CanStateMachine.State.Ident.CanIntroWithoutFlowIntro::class -> CanStateMachine.State.Ident.CanIntroWithoutFlowIntro({ _, _ -> }, null)
            CanStateMachine.State.Ident.CanInput::class -> CanStateMachine.State.Ident.CanInput({ _, _ -> }, null)
            CanStateMachine.State.Ident.CanInputRetry::class -> CanStateMachine.State.Ident.CanInputRetry({ _, _ -> }, "")
            CanStateMachine.State.Ident.PinInput::class -> CanStateMachine.State.Ident.PinInput({ _, _ -> }, "")
            CanStateMachine.State.Ident.CanAndPinEntered::class -> CanStateMachine.State.Ident.CanAndPinEntered({ _, _ -> }, "", "")

            else -> throw IllegalArgumentException()
        }
    }
}
