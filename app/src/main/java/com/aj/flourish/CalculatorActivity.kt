package com.aj.flourish

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aj.flourish.Utils.BadgeManager
import com.aj.flourish.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class CalculatorActivity : BaseActivity() {
    private lateinit var tvResult: TextView
    private lateinit var tvHistory: TextView
    private var currentInput = StringBuilder()
    private var currentResult = ""
    private var lastNumeric = false
    private var lastDot = false
    private var lastOperator = false
    private var useCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tvHistory = findViewById(R.id.tvHistory)
        tvResult = findViewById(R.id.tvResult)

        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnDecimal, R.id.btnClear, R.id.btnEquals,
            R.id.btnBracketOpen, R.id.btnBracketClose, R.id.btnBackspace
        )

        buttons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener { onButtonClick(it) }
        }
    }

    private fun onButtonClick(view: View) {
        when (view.id) {
            R.id.btnClear -> clearAll()
            R.id.btnEquals -> calculateResult()
            R.id.btnDecimal -> addDecimal()
            R.id.btnBackspace -> backspace()
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide -> addOperator((view as Button).text.toString())
            R.id.btnBracketOpen, R.id.btnBracketClose -> addBracket((view as Button).text.toString())
            else -> addDigit((view as Button).text.toString())
        }
    }

    private fun addDigit(digit: String) {
        if (currentInput.isEmpty() && digit == "0") return
        currentInput.append(digit)
        updateDisplay()
        lastNumeric = true
        lastDot = false
        lastOperator = false
    }

    private fun addDecimal() {
        if (lastDot || !lastNumeric) return
        currentInput.append(".")
        updateDisplay()
        lastDot = true
        lastNumeric = false
        lastOperator = false
    }

    private fun addOperator(operator: String) {
        if (currentInput.isEmpty() && operator == "-") {
            currentInput.append(operator)
            updateDisplay()
            lastNumeric = false
            lastDot = false
            lastOperator = true
            return
        }
        if (currentInput.isEmpty() || lastOperator) return
        currentInput.append(operator)
        updateDisplay()
        lastNumeric = false
        lastDot = false
        lastOperator = true
    }

    private fun addBracket(bracket: String) {
        currentInput.append(bracket)
        updateDisplay()
    }

    private fun clearAll() {
        currentInput.clear()
        currentResult = ""
        tvResult.text = "0"
        tvHistory.text = ""
        lastNumeric = false
        lastDot = false
        lastOperator = false
    }

    private fun backspace() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }
    }

    private fun calculateResult() {
        if (currentInput.isEmpty() || lastOperator) return
        try {
            val expression = currentInput.toString()
                .replace("×", "*")
                .replace("÷", "/")

            val result = eval(expression)
            currentResult = result.toString()
            tvHistory.text = currentInput.toString()
            tvResult.text = currentResult
            currentInput.clear()
            currentInput.append(currentResult)

            lastNumeric = true
            lastDot = currentResult.contains(".")
            lastOperator = false

            useCount++
            if (useCount == 3) {
                CoroutineScope(Dispatchers.IO).launch {
                    BadgeManager.checkAndUnlockBadge(this@CalculatorActivity, "calculator_used_3_times")
                }
            }
        } catch (e: Exception) {
            tvResult.text = "Error"
        }
    }

    private fun updateDisplay() {
        tvResult.text = currentInput.toString()
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                return x
            }
        }.parse()
    }
}
