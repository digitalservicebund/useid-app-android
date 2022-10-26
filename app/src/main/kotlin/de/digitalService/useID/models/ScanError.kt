package de.digitalService.useID.models

import de.digitalService.useID.R

sealed class ScanError {
    data class IncorrectPIN(val attempts: Int) : ScanError()
    object PINSuspended : ScanError()
    object PINBlocked : ScanError()
    object CardDeactivated : ScanError()
    data class CardErrorWithRedirect(val redirectUrl: String) : ScanError()
    object CardErrorWithoutRedirect : ScanError()
    data class Other(val message: String?) : ScanError()

    val titleResID: Int
        get() {
            return when (this) {
                PINSuspended -> R.string.scanError_cardSuspended_title
                PINBlocked -> R.string.scanError_cardBlocked_title
                CardDeactivated -> R.string.scanError_cardDeactivated_title
                CardErrorWithoutRedirect, is CardErrorWithRedirect -> R.string.scanError_cardUnreadable_title
                is Other -> R.string.scanError_unknown_title
                else -> throw IllegalArgumentException()
            }
        }

    val textResID: Int
        get() {
            return when (this) {
                PINSuspended -> R.string.scanError_cardSuspended_body
                PINBlocked -> R.string.scanError_cardBlocked_body
                CardDeactivated -> R.string.scanError_cardDeactivated_body
                CardErrorWithoutRedirect, is CardErrorWithRedirect -> R.string.scanError_cardUnreadable_body
                is Other -> R.string.scanError_unknown_body
                else -> throw IllegalArgumentException()
            }
        }
}
