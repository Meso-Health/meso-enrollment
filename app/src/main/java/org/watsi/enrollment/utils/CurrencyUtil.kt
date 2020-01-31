package org.watsi.enrollment.utils

import android.content.Context
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import java.math.BigDecimal
import kotlin.math.log10

object CurrencyUtil {
    fun formatMoneyWithCurrency(context: Context, amount: Int): String {
        return String.format(
            context.getString(R.string.price_with_currency),
            BuildConfig.CURRENCY,
            formatMoney(amount)
        )
    }

    /**
     * moneyMultiple is defined as follows:
     *   lowest money value * moneyMultiple = 1
     *
     * For example:
     * - in Uganda, the lowest money value is 1 UGX. So the moneyMultiple is 1.
     * - in the United States, the lowest money value is 0.01 USD (one cent), so the moneyMultiple is 100.
     * For reasons of accounting consistentcy and best engineering practices, we store currency in our database as an integer.
     * As a result, in order to store one cent, we would store 1 in the database instead of 0.01.
     */
    fun formatMoney(amount: Int, moneyMultiple: Int = BuildConfig.MONEY_MULTIPLE): String {
        // The scale is the number of digits to the right of the decimal point.
        val scale  = log10(moneyMultiple.toDouble()).toInt()
        return BigDecimal(amount).setScale(scale).divide(BigDecimal(moneyMultiple)).toString()
    }

    /**
     * Parses money String as integer value of the lowest denomination of the currency
     */
    fun parseMoney(amount: String, moneyMultiple: Int = BuildConfig.MONEY_MULTIPLE): Int {
        return BigDecimal(amount).multiply(BigDecimal(moneyMultiple)).toInt()
    }
}
