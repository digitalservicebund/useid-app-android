package de.digitalService.useID.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager

fun Context.isScreenReaderOn(): Boolean {
    val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (accessibilityManager != null && accessibilityManager.isEnabled) {
        val serviceInfoList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        return serviceInfoList.isNotEmpty()
    }

    return false
}
