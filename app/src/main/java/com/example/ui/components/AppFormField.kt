package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MilkBlue

/**
 * Standard Design System Form Field conforming strictly to the reference image:
 * - Clear, readable uppercase label positioned outside above the input box (never overlapping or clipped).
 * - Distinct, roomy container with smooth rounded corners (12.dp) and clean subtle border.
 * - Clean interior padding with touch-friendly 52.dp minimum height.
 * - Crystal-clear, readable primary typography and soft placeholder text.
 * - Optional trailing icon (dropdown chevron, date picker icon, etc.).
 */
@Composable
fun AppFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    minHeight: Dp = 52.dp,
    labelBadge: String? = null,
    accentColor: Color = MilkBlue,
    isHighlighted: Boolean = false,
    errorMessage: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
    ) {
        // Label Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color(0xFF475569) // Crisp slate
            )

            if (labelBadge != null) {
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = labelBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }

        // Input Container Box (Matches Reference Image Style)
        val borderColor = when {
            errorMessage != null -> MaterialTheme.colorScheme.error
            isHighlighted -> accentColor
            else -> Color(0xFFCBD5E1) // Slate 300 subtle border
        }

        val backgroundColor = when {
            !enabled -> Color(0xFFF1F5F9)
            isHighlighted -> accentColor.copy(alpha = 0.04f)
            else -> Color(0xFFF8FAFC) // Very soft off-white fill
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isHighlighted || errorMessage != null) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled && onClick == null,
                        readOnly = readOnly || onClick != null,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        minLines = minLines,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (enabled) Color(0xFF0F172A) else Color(0xFF64748B)
                        ),
                        cursorBrush = SolidColor(accentColor),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions
                    )
                }

                if (trailingIcon != null) {
                    Box(
                        modifier = Modifier.padding(start = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        trailingIcon()
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
