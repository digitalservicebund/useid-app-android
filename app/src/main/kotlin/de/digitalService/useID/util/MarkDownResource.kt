package de.digitalService.useID.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun markDownResource(@StringRes id: Int): String {
    val rawString = stringResource(id = id)

    return rawString.replace(" \n", "\n")
}
