package de.digitalService.useID.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

enum class BundCardType {
    INFO, WARNING, ERROR, SUCCESS
}

@Composable
fun BundCard(type: BundCardType, title: String, body: String) {
    val containerColor: Color
    val icon: ImageVector
    val iconTint: Color

    when (type) {
        BundCardType.INFO -> {
            containerColor = UseIdTheme.colors.blue200
            icon = Icons.Filled.Info
            iconTint = UseIdTheme.colors.blue700
        }
        BundCardType.WARNING -> {
            containerColor = UseIdTheme.colors.yellow200
            icon = Icons.Filled.Warning
            iconTint = UseIdTheme.colors.orange400
        }
        BundCardType.ERROR -> {
            containerColor = UseIdTheme.colors.red200
            icon = Icons.Filled.Dangerous
            iconTint = UseIdTheme.colors.red900
        }
        BundCardType.SUCCESS -> {
            containerColor = UseIdTheme.colors.green100
            icon = Icons.Filled.CheckCircle
            iconTint = UseIdTheme.colors.green800
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = UseIdTheme.shapes.roundedLarge
    ) {
        Column(
            modifier = Modifier
                .padding(UseIdTheme.spaces.s)
        ) {
            val iconSize = 26.dp
            val iconTextSpacerWidth = 6.dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    modifier = Modifier
                        .size(iconSize),
                    tint = iconTint
                )
                Spacer(modifier = Modifier.width(iconTextSpacerWidth))
                Text(
                    text = title,
                    style = UseIdTheme.typography.bodyMBold,
                    color = UseIdTheme.colors.black
                )
            }

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xxs))

            Row(horizontalArrangement = Arrangement.Start) {
                Spacer(modifier = Modifier.width(iconSize + iconTextSpacerWidth))
                Text(
                    text = body,
                    style = UseIdTheme.typography.bodyMRegular,
                    color = UseIdTheme.colors.black
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewInfoCard() {
    UseIdTheme {
        BundCard(type = BundCardType.INFO, title = "Informational message", body = "Body text describing alert.")
    }
}

@Preview
@Composable
private fun PreviewWarningCard() {
    UseIdTheme {
        BundCard(type = BundCardType.WARNING, title = "Warning message", body = "Body text describing alert.")
    }
}

@Preview
@Composable
private fun PreviewErrorCard() {
    UseIdTheme {
        BundCard(type = BundCardType.ERROR, title = "Error message", body = "Body text describing alert.")
    }
}

@Preview
@Composable
private fun PreviewSuccessCard() {
    UseIdTheme {
        BundCard(type = BundCardType.SUCCESS, title = "Success message", body = "Body text describing alert.")
    }
}
