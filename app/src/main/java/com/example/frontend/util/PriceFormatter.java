package com.example.frontend.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);

    static {
        // Set currency symbol to VNĐ
        CURRENCY_FORMATTER.setCurrency(java.util.Currency.getInstance("VND"));
    }

    /**
     * Format price to Vietnamese currency format
     * @param price The price to format
     * @return Formatted price string (e.g., "150.000 ₫")
     */
    public static String format(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }

        try {
            return CURRENCY_FORMATTER.format(price);
        } catch (Exception e) {
            // Fallback to simple format
            return formatSimple(price);
        }
    }

    /**
     * Format price to Vietnamese currency format
     * @param price The price to format
     * @return Formatted price string (e.g., "150.000 ₫")
     */
    public static String format(double price) {
        return format(BigDecimal.valueOf(price));
    }

    /**
     * Format price to Vietnamese currency format
     * @param price The price to format
     * @return Formatted price string (e.g., "150.000 ₫")
     */
    public static String format(long price) {
        return format(BigDecimal.valueOf(price));
    }

    /**
     * Format price to Vietnamese currency format
     * @param price The price to format
     * @return Formatted price string (e.g., "150.000 ₫")
     */
    public static String format(int price) {
        return format(BigDecimal.valueOf(price));
    }

    /**
     * Simple format without currency symbol
     * @param price The price to format
     * @return Formatted price string (e.g., "150.000 VNĐ")
     */
    public static String formatSimple(BigDecimal price) {
        if (price == null) {
            return "0 VNĐ";
        }

        // Format with thousand separators
        NumberFormat formatter = NumberFormat.getNumberInstance(VIETNAM_LOCALE);
        return formatter.format(price) + " VNĐ";
    }

    /**
     * Format price with custom suffix
     * @param price The price to format
     * @param suffix The suffix to append
     * @return Formatted price string (e.g., "150.000 VNĐ")
     */
    public static String formatWithSuffix(BigDecimal price, String suffix) {
        if (price == null) {
            return "0 " + suffix;
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(VIETNAM_LOCALE);
        return formatter.format(price) + " " + suffix;
    }

    /**
     * Format price for display in lists (shorter format)
     * @param price The price to format
     * @return Formatted price string (e.g., "150K ₫")
     */
    public static String formatShort(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }

        BigDecimal thousand = new BigDecimal("1000");
        BigDecimal million = new BigDecimal("1000000");

        if (price.compareTo(million) >= 0) {
            BigDecimal millions = price.divide(million, 1, BigDecimal.ROUND_HALF_UP);
            return millions.stripTrailingZeros().toPlainString() + "M ₫";
        } else if (price.compareTo(thousand) >= 0) {
            BigDecimal thousands = price.divide(thousand, 1, BigDecimal.ROUND_HALF_UP);
            return thousands.stripTrailingZeros().toPlainString() + "K ₫";
        } else {
            return format(price);
        }
    }

    /**
     * Parse price string to BigDecimal
     * @param priceString The price string to parse
     * @return BigDecimal price or null if parsing fails
     */
    public static BigDecimal parse(String priceString) {
        if (priceString == null || priceString.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and spaces
            String cleanPrice = priceString.replaceAll("[₫VND\\s,]", "");
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Check if price is valid
     * @param price The price to check
     * @return true if price is valid and positive
     */
    public static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Check if price string is valid
     * @param priceString The price string to check
     * @return true if price string is valid
     */
    public static boolean isValidPriceString(String priceString) {
        return parse(priceString) != null;
    }
}
