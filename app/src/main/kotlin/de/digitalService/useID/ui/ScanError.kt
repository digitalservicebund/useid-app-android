package de.digitalService.useID.ui

import de.digitalService.useID.R

sealed class ScanError {
    data class IncorrectPIN(val attempts: Int) : ScanError()
    object PINSuspended : ScanError()
    object PINBlocked : ScanError()
    object CardBlocked : ScanError()
    object CardDeactivated : ScanError()
    data class Other(val message: String?) : ScanError()

    val titleResID: Int
        get() {
            return when (this) {
                PINSuspended, PINBlocked -> R.string.idScan_error_unknown_title
                CardDeactivated -> R.string.idScan_error_cardDeactivated_title
                is Other -> R.string.idScan_error_unknown_title
                else -> throw IllegalArgumentException()
            }
        }

    val textResID: Int
        get() {
            return when (this) {
                PINSuspended, PINBlocked -> R.string.idScan_error_unknown_body
                CardDeactivated -> R.string.idScan_error_cardDeactivated_body
                is Other -> R.string.idScan_error_unknown_body
                else -> throw IllegalArgumentException()
            }
        }
}
