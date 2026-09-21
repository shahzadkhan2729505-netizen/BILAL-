package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.ui.theme.MilkSky

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val calcState by viewModel.calcState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var isUltraFullscreen by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppGradients.ScreenBackground)
            .padding(if (isUltraFullscreen) 6.dp else 12.dp)
            .testTag("calculator_screen")
    ) {
        // Top Header Bar
        if (!isUltraFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brush = AppGradients.RoyalHero),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dairy Pro Calculator",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Full Screen • Milk Density 1.030 • Financial",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isUltraFullscreen = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Ultra Full Screen",
                            tint = MilkBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        } else {
            // Minimalist back from ultra full screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Full-Screen Dairy Calculator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { isUltraFullscreen = false },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Ultra Full Screen",
                        tint = MilkBlue
                    )
                }
            }
        }

        // Milk Quick Conversion Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MilkRibbonChip(
                label = "L ➔ KG",
                sub = "× 1.030",
                modifier = Modifier.weight(1f)
            ) { viewModel.onCalcLToKg() }

            MilkRibbonChip(
                label = "KG ➔ L",
                sub = "÷ 1.030",
                modifier = Modifier.weight(1f)
            ) { viewModel.onCalcKgToL() }

            MilkRibbonChip(
                label = "+ / -",
                sub = "Negate",
                modifier = Modifier.weight(0.9f)
            ) { viewModel.onCalcNegate() }

            MilkRibbonChip(
                label = "%",
                sub = "Percent",
                modifier = Modifier.weight(0.8f)
            ) { viewModel.onCalcPercent() }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large High-Contrast Display Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isUltraFullscreen) 120.dp else 115.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top status inside display: memory indicator & copy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (calcState.hasMemory) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MilkAmber.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "M (${calcState.memoryValue})",
                                    color = MilkAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (copiedFeedback) "✓ Copied!" else "",
                            color = MilkGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(calcState.displayValue))
                            copiedFeedback = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Expression & Result Numbers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    if (calcState.expression.isNotEmpty()) {
                        Text(
                            text = calcState.expression,
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val valLen = calcState.displayValue.length
                    val fontSize = when {
                        valLen > 14 -> 24.sp
                        valLen > 10 -> 30.sp
                        valLen > 7 -> 36.sp
                        else -> 42.sp
                    }

                    Text(
                        text = calcState.displayValue,
                        color = Color.White,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Memory Ribbon Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("MC", "MR", "M+", "M-").forEach { mem ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clickable { viewModel.onCalcMemory(mem) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = mem,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad Grid - Expanding dynamically to fill the entire remaining vertical screen height!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, ⌫, %, ÷
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FullScreenCalcButton("AC", isClear = true, modifier = Modifier.weight(1f)) { viewModel.onCalcClear() }
                FullScreenCalcButton("⌫", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcBackspace() }
                FullScreenCalcButton("%", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcPercent() }
                FullScreenCalcButton("÷", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcOperator("÷") }
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FullScreenCalcButton("7", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("7") }
                FullScreenCalcButton("8", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("8") }
                FullScreenCalcButton("9", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("9") }
                FullScreenCalcButton("×", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcOperator("×") }
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FullScreenCalcButton("4", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("4") }
                FullScreenCalcButton("5", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("5") }
                FullScreenCalcButton("6", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("6") }
                FullScreenCalcButton("−", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcOperator("−") }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FullScreenCalcButton("1", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("1") }
                FullScreenCalcButton("2", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("2") }
                FullScreenCalcButton("3", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("3") }
                FullScreenCalcButton("+", isOp = true, modifier = Modifier.weight(1f)) { viewModel.onCalcOperator("+") }
            }

            // Row 5: 00, 0, ., =
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FullScreenCalcButton("00", modifier = Modifier.weight(1f)) {
                    viewModel.onCalcDigit("0")
                    viewModel.onCalcDigit("0")
                }
                FullScreenCalcButton("0", modifier = Modifier.weight(1f)) { viewModel.onCalcDigit("0") }
                FullScreenCalcButton(".", modifier = Modifier.weight(1f)) { viewModel.onCalcDecimal() }
                FullScreenCalcButton("=", isEqual = true, modifier = Modifier.weight(1f)) { viewModel.onCalcEquals() }
            }
        }
    }
}

@Composable
fun MilkRibbonChip(
    label: String,
    sub: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MilkNavy
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = sub,
                fontSize = 9.sp,
                color = MilkSky,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FullScreenCalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,
    isClear: Boolean = false,
    isEqual: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isEqual -> MilkGreen
        isClear -> MilkRed
        isOp -> MilkBlue
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isEqual || isClear || isOp -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isEqual || isOp) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxHeight()
            .testTag("calc_btn_$text")
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 1 && !text.equals("00")) 20.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,
    isClear: Boolean = false,
    isEqual: Boolean = false,
    onClick: () -> Unit
) {
    FullScreenCalcButton(
        text = text,
        modifier = modifier,
        isOp = isOp,
        isClear = isClear,
        isEqual = isEqual,
        onClick = onClick
    )
}
