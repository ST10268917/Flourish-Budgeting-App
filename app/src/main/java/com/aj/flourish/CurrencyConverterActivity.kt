package com.aj.flourish

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.aj.flourish.network.ExchangeRateResponse
import com.aj.flourish.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyConverterActivity : AppCompatActivity() {
    private val TAG = "CurrencyConverter"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_converter)

        val baseCurrencySpinner: Spinner = findViewById(R.id.spinner_base_currency)
        val quoteCurrencySpinner: Spinner = findViewById(R.id.spinner_quote_currency)
        val amountEditText: EditText = findViewById(R.id.edit_text_amount)
        val convertButton: Button = findViewById(R.id.button_convert)
        val resultTextView: TextView = findViewById(R.id.text_view_result)

        val currencies = listOf("ZAR", "USD", "GBP", "EUR")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        baseCurrencySpinner.adapter = adapter
        quoteCurrencySpinner.adapter = adapter

        convertButton.setOnClickListener {
            val baseCurrency = baseCurrencySpinner.selectedItem.toString()
            val quoteCurrency = quoteCurrencySpinner.selectedItem.toString()
            val amountText = amountEditText.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()
                fetchExchangeRate(baseCurrency, quoteCurrency, amount, resultTextView)
            } else {
                resultTextView.text = "Please enter an amount."
            }
        }
    }

    private fun fetchExchangeRate(
        baseCurrency: String,
        quoteCurrency: String,
        amount: Double,
        resultTextView: TextView
    ) {
        val call = RetrofitClient.instance.getExchangeRates(
            BuildConfig.EXCHANGERATE_API_KEY,
            baseCurrency
        )
        Log.d(TAG, "API Key: ${BuildConfig.EXCHANGERATE_API_KEY}")

        call.enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(
                call: Call<ExchangeRateResponse>,
                response: Response<ExchangeRateResponse>
            ) {
                if (response.isSuccessful) {
                    val exchangeRateResponse = response.body()
                    exchangeRateResponse?.let {
                        val rate = it.conversion_rates[quoteCurrency]
                        if (rate != null) {
                            val convertedAmount = amount * rate
                            resultTextView.text =
                                "$amount $baseCurrency = ${"%.2f".format(convertedAmount)} $quoteCurrency"
                        } else {
                            resultTextView.text = "Currency not found."
                        }
                    }
                } else {
                    resultTextView.text = "Conversion failed: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                resultTextView.text = "Error: ${t.localizedMessage}"
            }
        })
    }
}
