package de.digitalService.useID.util

import de.digitalService.useID.flows.PinManagementStateMachine
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class PinManagementStateFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): PinManagementStateMachine.State {
        return when (what) {
            PinManagementStateMachine.State.Invalid::class -> PinManagementStateMachine.State.Invalid

            PinManagementStateMachine.State.OldTransportPinInput::class -> PinManagementStateMachine.State.OldTransportPinInput(false)
            PinManagementStateMachine.State.OldPersonalPinInput::class -> PinManagementStateMachine.State.OldPersonalPinInput
            PinManagementStateMachine.State.NewPinIntro::class -> PinManagementStateMachine.State.NewPinIntro(false, false, "")
            PinManagementStateMachine.State.NewPinInput::class -> PinManagementStateMachine.State.NewPinInput(false, false, "")
            PinManagementStateMachine.State.NewPinConfirmation::class -> PinManagementStateMachine.State.NewPinConfirmation(false, false, "", "")
            PinManagementStateMachine.State.ReadyForScan::class -> PinManagementStateMachine.State.ReadyForScan(false, false, "", "")
            PinManagementStateMachine.State.WaitingForFirstCardAttachment::class -> PinManagementStateMachine.State.WaitingForFirstCardAttachment(false, false, "", "")
            PinManagementStateMachine.State.WaitingForCardReAttachment::class -> PinManagementStateMachine.State.WaitingForCardReAttachment(false, false, "", "")
            PinManagementStateMachine.State.FrameworkReadyForPinManagement::class -> PinManagementStateMachine.State.FrameworkReadyForPinManagement(false, false, "", "", {_, _ -> })
            PinManagementStateMachine.State.CanRequested::class -> PinManagementStateMachine.State.CanRequested(false, false, "", "", false)
            PinManagementStateMachine.State.Finished::class -> PinManagementStateMachine.State.Finished
            PinManagementStateMachine.State.Cancelled::class -> PinManagementStateMachine.State.Cancelled
            PinManagementStateMachine.State.OldTransportPinRetry::class -> PinManagementStateMachine.State.OldTransportPinRetry(false, "", {_, _ -> })
            PinManagementStateMachine.State.OldPersonalPinRetry::class -> PinManagementStateMachine.State.OldPersonalPinRetry("", {_, _ -> })

            PinManagementStateMachine.State.CardDeactivated::class -> PinManagementStateMachine.State.CardDeactivated
            PinManagementStateMachine.State.CardBlocked::class -> PinManagementStateMachine.State.CardBlocked
            PinManagementStateMachine.State.ProcessFailed::class -> PinManagementStateMachine.State.ProcessFailed(false, false, "", "", false)
            PinManagementStateMachine.State.UnknownError::class -> PinManagementStateMachine.State.UnknownError

            else -> throw IllegalArgumentException()
        }
    }
}
