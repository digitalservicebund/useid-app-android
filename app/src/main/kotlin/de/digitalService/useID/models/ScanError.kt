package de.digitalService.useID.models

import de.digitalService.useID.R

sealed class ScanError {
    data class IncorrectPin(val attempts: Int) : ScanError()
    object PinSuspended : ScanError()
    object PinBlocked : ScanError()
    object CardDeactivated : ScanError()
    data class CardErrorWithRedirect(val redirectUrl: String) : ScanError()
    object CardErrorWithoutRedirect : ScanError()
    data class Other(val message: String?) : ScanError()

    val titleResID: Int
        get() {
            return when (this) {
                PinSuspended -> R.string.scanError_cardSuspended_title
                PinBlocked -> R.string.scanError_cardBlocked_title
                CardDeactivated -> R.string.scanError_cardDeactivated_title
                CardErrorWithoutRedirect, is CardErrorWithRedirect -> R.string.scanError_cardUnreadable_title
                is Other -> R.string.scanError_unknown_title
                else -> throw IllegalArgumentException()
            }
        }

    val textResID: Int
        get() {
            return when (this) {
                PinSuspended -> R.string.scanError_cardSuspended_body
                PinBlocked -> R.string.scanError_cardBlocked_body
                CardDeactivated -> R.string.scanError_cardDeactivated_body
                CardErrorWithoutRedirect, is CardErrorWithRedirect -> R.string.scanError_cardUnreadable_body
                is Other -> R.string.scanError_unknown_body
                else -> throw IllegalArgumentException()
            }
        }
}
