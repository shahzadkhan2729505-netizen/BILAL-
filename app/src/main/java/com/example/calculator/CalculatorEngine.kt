package com.example.calculator

import java.util.Locale
import kotlin.math.roundToLong

data class CalculatorState(
    val displayValue: String = "0",
    val expression: String = "",
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

class CalculatorEngine {
    private var current: String = "0"
    private var previous: Double? = null
    private var operation: String? = null
    private var waitingForOperand: Boolean = false
    private var memory: Double = 0.0

    fun getState(): CalculatorState {
        val expr = if (previous != null && operation != null) {
            "${formatNumber(previous!!)} $operation"
        } else ""
        return CalculatorState(
            displayValue = current,
            expression = expr,
            memoryValue = memory,
            hasMemory = memory != 0.0
        )
    }

    fun onDigit(digit: String): CalculatorState {
        if (waitingForOperand || current == "Error") {
            current = digit
            waitingForOperand = false
        } else {
            current = if (current == "0") digit else current + digit
        }
        if (current.length > 14) current = current.substring(0, 14)
        return getState()
    }

    fun onDecimal(): CalculatorState {
        if (waitingForOperand || current == "Error") {
            current = "0."
            waitingForOperand = false
        } else if (!current.contains(".")) {
            current += "."
        }
        return getState()
    }

    fun onOperator(op: String): CalculatorState {
        if (current == "Error") return getState()
        val value = current.toDoubleOrNull() ?: 0.0
        if (previous != null && operation != null && !waitingForOperand) {
            compute(value)
        } else {
            previous = value
        }
        operation = op
        waitingForOperand = true
        return getState()
    }

    fun onEquals(): CalculatorState {
        if (operation == null || previous == null) return getState()
        val value = current.toDoubleOrNull() ?: 0.0
        compute(value)
        previous = null
        operation = null
        waitingForOperand = true
        return getState()
    }

    private fun compute(secondOperand: Double) {
        val first = previous ?: 0.0
        val result: Double = when (operation) {
            "+" -> first + secondOperand
            "−", "-" -> first - secondOperand
            "×", "*" -> first * secondOperand
            "÷", "/" -> if (secondOperand == 0.0) Double.NaN else first / secondOperand
            else -> secondOperand
        }

        if (result.isNaN() || result.isInfinite()) {
            current = "Error"
            previous = null
            operation = null
            waitingForOperand = true
        } else {
            // Round to avoid float precision weirdness
            val rounded = (result * 1e10).roundToLong() / 1e10
            current = formatNumber(rounded)
            previous = rounded
        }
    }

    fun onClear(): CalculatorState {
        current = "0"
        previous = null
        operation = null
        waitingForOperand = false
        return getState()
    }

    fun onBackspace(): CalculatorState {
        if (waitingForOperand || current == "Error") {
            return onClear()
        }
        current = if (current.length > 1) current.dropLast(1) else "0"
        return getState()
    }

    fun onPercent(): CalculatorState {
        val n = current.toDoubleOrNull() ?: return getState()
        val r = n / 100.0
        current = formatNumber(r)
        return getState()
    }

    fun onNegate(): CalculatorState {
        if (current == "0" || current == "Error") return getState()
        current = if (current.startsWith("-")) current.substring(1) else "-$current"
        return getState()
    }

    fun onMilkConvertKgToL(): CalculatorState {
        val kg = current.toDoubleOrNull() ?: return getState()
        val liters = kg / 1.030
        current = formatNumber((liters * 1000).roundToLong() / 1000.0)
        return getState()
    }

    fun onMilkConvertLToKg(): CalculatorState {
        val liters = current.toDoubleOrNull() ?: return getState()
        val kg = liters * 1.030
        current = formatNumber((kg * 1000).roundToLong() / 1000.0)
        return getState()
    }

    fun onMemory(action: String): CalculatorState {
        val n = current.toDoubleOrNull() ?: 0.0
        when (action) {
            "MC" -> memory = 0.0
            "MR" -> {
                current = formatNumber(memory)
                waitingForOperand = false
            }
            "M+" -> memory += n
            "M-" -> memory -= n
        }
        return getState()
    }

    private fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0 && num >= Long.MIN_VALUE && num <= Long.MAX_VALUE) {
            num.toLong().toString()
        } else {
            val s = String.format(Locale.US, "%.8f", num).trimEnd('0').trimEnd('.')
            if (s.isEmpty()) "0" else s
        }
    }
}
