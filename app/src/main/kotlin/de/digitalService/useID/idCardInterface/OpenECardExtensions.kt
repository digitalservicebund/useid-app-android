package de.digitalService.useID.idCardInterface

import org.openecard.mobile.activation.SelectableItem
import org.openecard.mobile.activation.ServiceErrorResponse

fun ServiceErrorResponse.errorDescription(): String = "${statusCode.ordinal} - ${statusCode.name}: $errorMessage"

fun List<SelectableItem>.reduceToMap(): Map<IdCardAttribute, Boolean> = map { it ->
    try {
        Pair(IdCardAttribute.valueOf(it.name), it.isRequired)
    } catch (e: IllegalArgumentException) {
        throw IdCardInteractionException.UnexpectedReadAttribute(e.message)
    }
}.toMap()

fun Map<IdCardAttribute, Boolean>.toSelectableItems(): List<SelectableItem> = map {
    object :
        SelectableItem {
        override fun getName(): String = it.key.name
        override fun getText(): String = ""
        override fun isChecked(): Boolean = it.value
        override fun setChecked(p0: Boolean) { }
        override fun isRequired(): Boolean = false
    }
}
