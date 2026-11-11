package com.drivego.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility to format BigDecimal values as Indian rupee strings.
 * Example: 3200.00 → ₹3,200
 */
public final class PriceFormatter {

    private static final Locale INDIA = new Locale("en", "IN");

    private PriceFormatter() {}

    public static String format(BigDecimal amount) {
        if (amount == null) return "₹0";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(INDIA);
        return fmt.format(amount);
    }

    public static String formatPlain(BigDecimal amount) {
        if (amount == null) return "0";
        NumberFormat fmt = NumberFormat.getNumberInstance(INDIA);
        fmt.setMinimumFractionDigits(0);
        fmt.setMaximumFractionDigits(2);
        return fmt.format(amount);
    }
}
