package de.digitalService.useID.util

import de.digitalService.useID.flows.ChangePinStateMachine
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class PinManagementStateFactory : DefaultTypeFactory() {
    override fun create(what: KClass<*>): ChangePinStateMachine.State {
        return when (what) {
            ChangePinStateMachine.State.Invalid::class -> ChangePinStateMachine.State.Invalid

            ChangePinStateMachine.State.OldTransportPinInput::class -> ChangePinStateMachine.State.OldTransportPinInput(false)
            ChangePinStateMachine.State.OldPersonalPinInput::class -> ChangePinStateMachine.State.OldPersonalPinInput
            ChangePinStateMachine.State.NewPinIntro::class -> ChangePinStateMachine.State.NewPinIntro(false, false, "")
            ChangePinStateMachine.State.NewPinInput::class -> ChangePinStateMachine.State.NewPinInput(false, false, "")
            ChangePinStateMachine.State.NewPinConfirmation::class -> ChangePinStateMachine.State.NewPinConfirmation(false, false, "", "")
            ChangePinStateMachine.State.StartIdCardInteraction::class -> ChangePinStateMachine.State.StartIdCardInteraction(false, false, "", "")
            ChangePinStateMachine.State.ReadyForSubsequentScan::class -> ChangePinStateMachine.State.ReadyForSubsequentScan(false, false, "", "")
            ChangePinStateMachine.State.FrameworkReadyForPinInput::class -> ChangePinStateMachine.State.FrameworkReadyForPinInput(false, false, "", "")
            ChangePinStateMachine.State.FrameworkReadyForNewPinInput::class -> ChangePinStateMachine.State.FrameworkReadyForNewPinInput(false, false, "", "")
            ChangePinStateMachine.State.CanRequested::class -> ChangePinStateMachine.State.CanRequested(false, false, "", "", false)
            ChangePinStateMachine.State.Finished::class -> ChangePinStateMachine.State.Finished
            ChangePinStateMachine.State.Cancelled::class -> ChangePinStateMachine.State.Cancelled
            ChangePinStateMachine.State.OldTransportPinRetry::class -> ChangePinStateMachine.State.OldTransportPinRetry(false, "")
            ChangePinStateMachine.State.OldPersonalPinRetry::class -> ChangePinStateMachine.State.OldPersonalPinRetry("")

            ChangePinStateMachine.State.CardDeactivated::class -> ChangePinStateMachine.State.CardDeactivated
            ChangePinStateMachine.State.CardBlocked::class -> ChangePinStateMachine.State.CardBlocked
            ChangePinStateMachine.State.ProcessFailed::class -> ChangePinStateMachine.State.ProcessFailed(false, false, "", "", false)
            ChangePinStateMachine.State.UnknownError::class -> ChangePinStateMachine.State.UnknownError

            else -> throw IllegalArgumentException()
        }
    }
}
