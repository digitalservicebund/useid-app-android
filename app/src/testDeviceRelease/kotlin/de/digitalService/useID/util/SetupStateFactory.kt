package de.digitalService.useID.util

import de.digitalService.useID.flows.SetupStateMachine
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class SetupStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): SetupStateMachine.State {
        return when (what) {
            SetupStateMachine.State.Invalid::class -> SetupStateMachine.State.Invalid

            SetupStateMachine.State.Intro::class -> SetupStateMachine.State.Intro(null)
            SetupStateMachine.State.SkippingToIdentRequested::class -> SetupStateMachine.State.SkippingToIdentRequested("")
            SetupStateMachine.State.StartSetup::class -> SetupStateMachine.State.StartSetup(null)
            SetupStateMachine.State.PinReset::class -> SetupStateMachine.State.PinReset(null)
            SetupStateMachine.State.PinManagement::class -> SetupStateMachine.State.PinManagement(null)
            SetupStateMachine.State.PinManagementFinished::class -> SetupStateMachine.State.PinManagement(null)
            SetupStateMachine.State.IdentAfterFinishedSetupRequested::class -> SetupStateMachine.State.IdentAfterFinishedSetupRequested("")
            SetupStateMachine.State.AlreadySetUpConfirmation::class -> SetupStateMachine.State.AlreadySetUpConfirmation
            SetupStateMachine.State.SetupFinished::class -> SetupStateMachine.State.SetupFinished

            else -> throw IllegalArgumentException()
        }
    }
}
